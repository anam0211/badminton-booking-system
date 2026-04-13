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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
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
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private static final byte ACTIVE_KEY = 1;

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

            publishBookingCreatedEventsSafely(booking, createdDetails);
            return mapToResponse(booking);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.TIMESLOT_ALREADY_BOOKED);
        }
    }

    public BookingResponse getBookingByIdForViewer(Long bookingId, User viewer) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!canAccessBooking(viewer, booking)) {
            throw new AccessDeniedCustomException("Bạn không có quyền xem booking này.");
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

        throw new AccessDeniedCustomException("Bạn không có quyền xem danh sách booking này.");
    }

    public BookingResponse cancelBookingForViewer(Long bookingId, User viewer) {
        if (!hasAdminAccess(viewer)) {
            throw new AccessDeniedCustomException("Bạn không có quyền hủy booking.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!canAccessBooking(viewer, booking)) {
            throw new AccessDeniedCustomException("Bạn không có quyền hủy booking này.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return mapToResponse(booking);
        }

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBooking_Id(bookingId);
        ensureCancellationAllowed(bookingDetails);

        booking.setTotalAmount(BigDecimal.ZERO);
        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        for (BookingDetail bookingDetail : bookingDetails) {
            bookingDetail.setActiveKey(null);
        }
        bookingDetailRepository.saveAll(bookingDetails);

        publishBookingCancelledEventsSafely(booking, bookingDetails);
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

        return RoleName.ADMIN.name().equalsIgnoreCase(user.getRole().getName());
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
                    booking.getId(),
                    bookingDetail.getCourt().getName(),
                    bookingDetail.getPlayDate().toString(),
                    formatTimeSlot(bookingDetail.getTimeSlot())
            ));
        }
    }

    private void publishBookingCreatedEventsSafely(Booking booking, List<BookingDetail> bookingDetails) {
        try {
            publishBookingCreatedEvents(booking, bookingDetails);
        } catch (RuntimeException ex) {
            log.warn("Không thể gửi thông báo sau khi tạo booking {}", booking != null ? booking.getId() : null, ex);
        }
    }

    private void publishBookingCancelledEvents(Booking booking, List<BookingDetail> bookingDetails) {
        if (booking == null || booking.getUser() == null || booking.getUser().getId() == null) {
            return;
        }

        Long userId = booking.getUser().getId();
        for (BookingDetail bookingDetail : bookingDetails) {
            eventPublisher.publishEvent(new BookingCancelEvent(
                    userId,
                    booking.getId(),
                    bookingDetail.getCourt().getName(),
                    bookingDetail.getPlayDate().toString(),
                    formatTimeSlot(bookingDetail.getTimeSlot())
            ));
        }
    }

    private void publishBookingCancelledEventsSafely(Booking booking, List<BookingDetail> bookingDetails) {
        try {
            publishBookingCancelledEvents(booking, bookingDetails);
        } catch (RuntimeException ex) {
            log.warn("Không thể gửi thông báo sau khi hủy booking {}", booking != null ? booking.getId() : null, ex);
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

        long minutesUntilStart = Duration.between(LocalDateTime.now(), earliestStart).toMinutes();
        if (minutesUntilStart < 30) {
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

        CancellationAvailability cancellationAvailability = resolveCancellationAvailability(booking, items);
        response.setCancellable(cancellationAvailability.cancellable());
        response.setCancellationReason(cancellationAvailability.reason());

        return response;
    }

    private CancellationAvailability resolveCancellationAvailability(Booking booking, List<BookingDetailResponse> items) {
        if (booking == null) {
            return new CancellationAvailability(false, "Không tìm thấy booking.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return new CancellationAvailability(false, "Booking đã bị hủy.");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            return new CancellationAvailability(false, "Booking đã hoàn thành.");
        }

        LocalDateTime earliestStart = items.stream()
                .filter(item -> item.getActive() != null && item.getActive() == ACTIVE_KEY)
                .filter(item -> item.getPlayDate() != null)
                .filter(item -> item.getStartTime() != null && !item.getStartTime().isBlank())
                .map(item -> LocalDateTime.of(item.getPlayDate(), LocalTime.parse(item.getStartTime())))
                .min(LocalDateTime::compareTo)
                .orElse(null);

        if (earliestStart == null) {
            return new CancellationAvailability(false, "Booking không còn khung giờ hợp lệ để hủy.");
        }

        long minutesUntilStart = Duration.between(LocalDateTime.now(), earliestStart).toMinutes();
        if (minutesUntilStart < 30) {
            return new CancellationAvailability(false, "Chỉ được hủy sân trước 30 phút so với giờ bắt đầu.");
        }

        return new CancellationAvailability(true, null);
    }

    private record CancellationAvailability(boolean cancellable, String reason) {
    }
}
