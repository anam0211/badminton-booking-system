package com.badminton.booking.booking.service;

import com.badminton.booking.booking.dto.request.BookingRequest;
import com.badminton.booking.booking.dto.request.Slots;
import com.badminton.booking.booking.dto.response.BookingDetailResponse;
import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.mapper.BookingDetailMapper;
import com.badminton.booking.booking.mapper.BookingMapper;
import com.badminton.booking.booking.repository.BookingDetailRepository;
import com.badminton.booking.booking.repository.BookingRepository;
import com.badminton.booking.booking.repository.CourtRepository;
import com.badminton.booking.booking.repository.TimeSlotRepository;
import com.badminton.booking.booking.validator.BookingValidator;
import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.enums.PaymentStatus;
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.exception.AccessDeniedCustomException;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.domain.entity.Booking;
import com.badminton.booking.domain.entity.BookingDetail;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.domain.entity.TimeSlot;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.notification.event.BookingCancelEvent;
import com.badminton.booking.notification.event.BookingSuccessEvent;
import com.badminton.booking.notification.event.PaymentSuccessEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {

    private static final byte ACTIVE_KEY = 1;
    private static final byte INACTIVE_KEY = 0;

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingMapper bookingMapper;
    private final BookingDetailMapper bookingDetailMapper;
    private final BookingValidator bookingValidator;
    private final BookingPricingService bookingPricingService;
    private final ApplicationEventPublisher eventPublisher;

    public BookingResponse createBooking(BookingRequest request, Long userId) {
        bookingValidator.validateCreate(request);

        User user = new User();
        user.setId(userId);

        Booking booking = Booking.builder()
                .user(user)
                .note(request.getNote())
                .status(BookingStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .totalAmount(BigDecimal.ZERO)
                .build();

        booking = bookingRepository.save(booking);

        BigDecimal total = BigDecimal.ZERO;
        List<BookingDetail> createdDetails = new ArrayList<>();

        try {
            for (Slots slot : request.getSlots()) {
                Court court = courtRepository.findById(slot.getCourtId())
                        .orElseThrow(() -> new AppException(ErrorCode.COURT_NOT_FOUND));

                if (!court.getBranch().getId().equals(request.getBranchId())) {
                    throw new AppException(ErrorCode.INVALID_REQUEST);
                }

                TimeSlot timeSlot = timeSlotRepository.findById(slot.getTimeSlotId())
                        .orElseThrow(() -> new AppException(ErrorCode.TIMESLOT_NOT_FOUND));

                if (slot.getPlayDate().isEqual(LocalDate.now())
                        && !timeSlot.getStartTime().isAfter(LocalTime.now())) {
                    throw new AppException(ErrorCode.INVALID_REQUEST);
                }

                boolean existed = bookingDetailRepository.existsByCourt_IdAndTimeSlot_IdAndPlayDateAndActiveKey(
                        slot.getCourtId(),
                        slot.getTimeSlotId(),
                        slot.getPlayDate(),
                        ACTIVE_KEY
                );

                if (existed) {
                    throw new AppException(ErrorCode.TIMESLOT_ALREADY_BOOKED);
                }

                BigDecimal price = bookingPricingService.calculate(slot.getCourtId(), slot.getTimeSlotId());

                BookingDetail bookingDetail = BookingDetail.builder()
                        .booking(booking)
                        .court(court)
                        .timeSlot(timeSlot)
                        .playDate(slot.getPlayDate())
                        .price(price)
                        .activeKey(ACTIVE_KEY)
                        .build();

                bookingDetailRepository.save(bookingDetail);
                createdDetails.add(bookingDetail);
                total = total.add(price);
            }

            booking.setTotalAmount(total);
            booking = bookingRepository.save(booking);

            publishBookingCreatedEvents(booking, createdDetails);
            return mapToResponse(booking);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.TIMESLOT_ALREADY_BOOKED);
        }
    }

    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        return mapToResponse(booking);
    }

    public BookingResponse getBookingByIdForViewer(Long bookingId, User viewer) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!canAccessBooking(viewer, booking)) {
            throw new AccessDeniedCustomException("B\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n xem booking n\u00e0y.");
        }

        return mapToResponse(booking);
    }

    public List<BookingResponse> getBookingHistory(Long userId) {
        return mapToResponses(bookingRepository.findBookingHistoryByUserId(userId));
    }

    public List<BookingResponse> getAllBookingsForViewer(User viewer) {
        if (isSystemAdmin(viewer)) {
            return mapToResponses(bookingRepository.findAllWithUserOrderByBookingDateDesc());
        }

        if (isBranchAdmin(viewer)) {
            Long managedBranchId = getManagedBranchId(viewer);
            if (managedBranchId == null) {
                return Collections.emptyList();
            }

            return mapToResponses(bookingRepository.findAllWithUserByBranchIdOrderByBookingDateDesc(managedBranchId));
        }

        throw new AccessDeniedCustomException("B\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n xem danh s\u00e1ch booking n\u00e0y.");
    }

    public BookingResponse cancelBookingForViewer(Long bookingId, User viewer) {
        if (!hasAdminAccess(viewer)) {
            throw new AccessDeniedCustomException("B\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n h\u1ee7y booking.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!canAccessBooking(viewer, booking)) {
            throw new AccessDeniedCustomException("B\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n h\u1ee7y booking n\u00e0y.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return mapToResponse(booking);
        }

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBooking_Id(bookingId);
        ensureCancellationAllowed(bookingDetails);

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        for (BookingDetail bookingDetail : bookingDetails) {
            bookingDetail.setActiveKey(INACTIVE_KEY);
        }
        bookingDetailRepository.saveAll(bookingDetails);

        publishBookingCancelledEvents(booking, bookingDetails);
        return mapToResponse(booking);
    }

    private boolean canAccessBooking(User viewer, Booking booking) {
        if (viewer == null || booking == null || booking.getUser() == null) {
            return false;
        }

        if (isSystemAdmin(viewer)) {
            return true;
        }

        if (isBranchAdmin(viewer)) {
            Long managedBranchId = getManagedBranchId(viewer);
            return managedBranchId != null
                    && bookingDetailRepository.existsByBooking_IdAndCourt_Branch_Id(booking.getId(), managedBranchId);
        }

        return viewer.getId() != null && viewer.getId().equals(booking.getUser().getId());
    }

    private boolean hasAdminAccess(User user) {
        return isSystemAdmin(user) || isBranchAdmin(user);
    }

    private boolean isSystemAdmin(User user) {
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            return false;
        }

        String roleName = user.getRole().getName();
        return RoleName.ADMIN.name().equalsIgnoreCase(roleName);
    }

    private boolean isBranchAdmin(User user) {
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            return false;
        }

        return RoleName.BRANCH_ADMIN.name().equalsIgnoreCase(user.getRole().getName());
    }

    private Long getManagedBranchId(User user) {
        if (user == null || user.getManagedBranch() == null) {
            return null;
        }

        return user.getManagedBranch().getId();
    }

    private void publishBookingCreatedEvents(Booking booking, List<BookingDetail> bookingDetails) {
        if (booking == null || booking.getUser() == null || booking.getUser().getId() == null) {
            return;
        }

        Long userId = booking.getUser().getId();
        for (BookingDetail bookingDetail : bookingDetails) {
            eventPublisher.publishEvent(new BookingSuccessEvent(
                    userId,
                    bookingDetail.getCourt().getName(),
                    bookingDetail.getPlayDate().toString(),
                    formatTimeSlot(bookingDetail.getTimeSlot())
            ));
        }

        eventPublisher.publishEvent(new PaymentSuccessEvent(
                userId,
                String.valueOf(booking.getId()),
                booking.getTotalAmount() != null ? booking.getTotalAmount().toPlainString() : "0"
        ));
    }

    private void publishBookingCancelledEvents(Booking booking, List<BookingDetail> bookingDetails) {
        if (booking == null || booking.getUser() == null || booking.getUser().getId() == null) {
            return;
        }

        Long userId = booking.getUser().getId();
        for (BookingDetail bookingDetail : bookingDetails) {
            eventPublisher.publishEvent(new BookingCancelEvent(
                    userId,
                    bookingDetail.getCourt().getName(),
                    bookingDetail.getPlayDate().toString(),
                    formatTimeSlot(bookingDetail.getTimeSlot())
            ));
        }
    }

    private void ensureCancellationAllowed(List<BookingDetail> bookingDetails) {
        if (bookingDetails == null || bookingDetails.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        LocalDateTime earliestStart = bookingDetails.stream()
                .filter(detail -> detail.getActiveKey() != null && detail.getActiveKey() == ACTIVE_KEY)
                .filter(detail -> detail.getPlayDate() != null)
                .filter(detail -> detail.getTimeSlot() != null && detail.getTimeSlot().getStartTime() != null)
                .map(detail -> LocalDateTime.of(detail.getPlayDate(), detail.getTimeSlot().getStartTime()))
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        LocalDateTime cancelDeadline = earliestStart.minusMinutes(30);
        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new AppException(ErrorCode.CANCELLATION_WINDOW_EXPIRED);
        }
    }

    private String formatTimeSlot(TimeSlot timeSlot) {
        if (timeSlot == null) {
            return "";
        }
        return timeSlot.getStartTime() + " - " + timeSlot.getEndTime();
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<BookingDetailResponse> items = bookingDetailRepository.findDetailedByBookingId(booking.getId())
                .stream()
                .map(bookingDetailMapper::toDto)
                .toList();
        return mapToResponse(booking, items);
    }

    private List<BookingResponse> mapToResponses(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> bookingIds = bookings.stream()
                .map(Booking::getId)
                .toList();

        Map<Long, List<BookingDetailResponse>> itemsByBookingId = new LinkedHashMap<>();
        for (BookingDetail bookingDetail : bookingDetailRepository.findDetailedByBookingIds(bookingIds)) {
            Long bookingId = bookingDetail.getBooking().getId();
            itemsByBookingId.computeIfAbsent(bookingId, ignored -> new ArrayList<>())
                    .add(bookingDetailMapper.toDto(bookingDetail));
        }

        return bookings.stream()
                .map(booking -> mapToResponse(
                        booking,
                        itemsByBookingId.getOrDefault(booking.getId(), Collections.emptyList())
                ))
                .toList();
    }

    private BookingResponse mapToResponse(Booking booking, List<BookingDetailResponse> items) {
        BookingResponse response = bookingMapper.toDto(booking);
        response.setItems(items);
        response.setTotalSlots(items.size());

        if (!items.isEmpty()) {
            response.setBranchId(items.get(0).getBranchId());
            response.setBranchName(items.get(0).getBranchName());
            response.setEarliestPlayDate(items.stream()
                    .map(BookingDetailResponse::getPlayDate)
                    .filter(java.util.Objects::nonNull)
                    .min(LocalDate::compareTo)
                    .orElse(null));
            response.setLatestPlayDate(items.stream()
                    .map(BookingDetailResponse::getPlayDate)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(null));
        }

        return response;
    }
}
