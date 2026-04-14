package com.badminton.booking.booking.service;

import com.badminton.booking.booking.dto.request.BookingRequest;
import com.badminton.booking.booking.dto.request.Slots;
import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.repository.BookingCourtRepository;
import com.badminton.booking.booking.repository.BookingDetailRepository;
import com.badminton.booking.booking.repository.BookingRepository;
import com.badminton.booking.booking.repository.BookingTimeSlotRepository;
import com.badminton.booking.booking.validator.BookingValidator;
import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.enums.CourtStatus;
import com.badminton.booking.common.exception.AccessDeniedCustomException;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.domain.entity.Booking;
import com.badminton.booking.domain.entity.BookingDetail;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.domain.entity.TimeSlot;
import com.badminton.booking.domain.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {

    private static final byte ACTIVE_KEY = 1;

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final BookingCourtRepository courtRepository;
    private final BookingTimeSlotRepository timeSlotRepository;
    private final BookingValidator bookingValidator;
    private final BookingPricingService bookingPricingService;
    private final BookingAccessService bookingAccessService;
    private final BookingResponseService bookingResponseService;
    private final BookingEventService bookingEventService;

    public BookingResponse createBooking(BookingRequest request, Long userId) {
        bookingValidator.validateCreate(request);

        Booking booking = createDraftBooking(request, userId);

        try {
            BookingCreationResult creationResult = createBookingDetails(booking, request);
            booking.setTotalAmount(creationResult.totalAmount());
            booking = bookingRepository.save(booking);

            bookingEventService.publishCreatedSafely(booking, creationResult.bookingDetails());
            return bookingResponseService.toResponse(booking);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.TIMESLOT_ALREADY_BOOKED);
        }
    }

    public BookingResponse getBookingByIdForViewer(Long bookingId, User viewer) {
        completeFinishedBookings();

        Booking booking = findBookingOrThrow(bookingId);
        if (!bookingAccessService.canViewBooking(viewer, booking)) {
            throw new AccessDeniedCustomException("Bạn không có quyền xem booking này.");
        }
        return bookingResponseService.toResponse(booking);
    }

    public List<BookingResponse> getBookingHistory(Long userId) {
        completeFinishedBookings();
        return bookingResponseService.toResponses(bookingRepository.findBookingHistoryByUserId(userId));
    }

    public List<BookingResponse> getAllBookingsForViewer(User viewer) {
        completeFinishedBookings();

        if (bookingAccessService.isSystemAdmin(viewer)) {
            return bookingResponseService.toResponses(bookingRepository.findAllWithUserOrderByBookingDateDesc());
        }

        if (bookingAccessService.isBranchAdmin(viewer)) {
            Long managedBranchId = bookingAccessService.getManagedBranchId(viewer);
            if (managedBranchId == null) {
                return Collections.emptyList();
            }

            return bookingResponseService.toResponses(
                    bookingRepository.findAllWithUserByBranchIdOrderByBookingDateDesc(managedBranchId)
            );
        }

        throw new AccessDeniedCustomException("Bạn không có quyền xem danh sách booking này.");
    }

    public BookingResponse cancelBookingForViewer(Long bookingId, User viewer) {
        if (!bookingAccessService.hasAdminAccess(viewer)) {
            throw new AccessDeniedCustomException("Bạn không có quyền hủy booking.");
        }

        Booking booking = findBookingOrThrow(bookingId);
        if (!bookingAccessService.canManageBookingAsAdmin(viewer, booking)) {
            throw new AccessDeniedCustomException("Bạn không có quyền hủy booking này.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return bookingResponseService.toResponse(booking);
        }

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBooking_Id(bookingId);
        ensureCancellationAllowed(bookingDetails);

        booking.setTotalAmount(BigDecimal.ZERO);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        bookingDetails.forEach(detail -> detail.setActiveKey(null));
        bookingDetailRepository.saveAll(bookingDetails);

        bookingEventService.publishCancelledSafely(booking, bookingDetails);
        return bookingResponseService.toResponse(booking);
    }

    public int completeFinishedBookings() {
        List<Booking> confirmedBookings = bookingRepository.findAllByStatusOrderByBookingDateAsc(BookingStatus.CONFIRMED);
        if (confirmedBookings.isEmpty()) {
            return 0;
        }

        List<Long> bookingIds = confirmedBookings.stream()
                .map(Booking::getId)
                .toList();
        Map<Long, List<BookingDetail>> detailsByBookingId = bookingDetailRepository.findDetailedByBookingIds(bookingIds)
                .stream()
                .collect(Collectors.groupingBy(detail -> detail.getBooking().getId()));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> completedBookings = confirmedBookings.stream()
                .filter(booking -> shouldMarkCompleted(detailsByBookingId.get(booking.getId()), now))
                .toList();

        if (completedBookings.isEmpty()) {
            return 0;
        }

        completedBookings.forEach(booking -> booking.setStatus(BookingStatus.COMPLETED));
        bookingRepository.saveAll(completedBookings);
        return completedBookings.size();
    }

    private Booking createDraftBooking(BookingRequest request, Long userId) {
        User user = new User();
        user.setId(userId);

        return bookingRepository.save(Booking.builder()
                .user(user)
                .note(request.getNote())
                .status(BookingStatus.CONFIRMED)
                .totalAmount(BigDecimal.ZERO)
                .build());
    }

    private BookingCreationResult createBookingDetails(Booking booking, BookingRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingDetail> bookingDetails = new ArrayList<>();

        for (Slots slot : request.getSlots()) {
            Court court = resolveCourtForBooking(slot.getCourtId(), request.getBranchId());
            TimeSlot timeSlot = resolveTimeSlot(slot.getTimeSlotId());

            ensureSlotCanBeBooked(slot, timeSlot);

            BigDecimal price = bookingPricingService.calculate(court, slot.getTimeSlotId());
            BookingDetail bookingDetail = bookingDetailRepository.save(BookingDetail.builder()
                    .booking(booking)
                    .court(court)
                    .timeSlot(timeSlot)
                    .playDate(slot.getPlayDate())
                    .price(price)
                    .activeKey(ACTIVE_KEY)
                    .build());

            bookingDetails.add(bookingDetail);
            totalAmount = totalAmount.add(price);
        }

        return new BookingCreationResult(bookingDetails, totalAmount);
    }

    private Court resolveCourtForBooking(Long courtId, Long branchId) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new AppException(ErrorCode.COURT_NOT_FOUND));

        if (!court.getBranch().getId().equals(branchId) || court.getStatus() != CourtStatus.AVAILABLE) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return court;
    }

    private TimeSlot resolveTimeSlot(Integer timeSlotId) {
        return timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new AppException(ErrorCode.TIMESLOT_NOT_FOUND));
    }

    private void ensureSlotCanBeBooked(Slots slot, TimeSlot timeSlot) {
        if (slot.getPlayDate() != null
                && slot.getPlayDate().isEqual(LocalDate.now())
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
    }

    private Booking findBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    private void ensureCancellationAllowed(List<BookingDetail> bookingDetails) {
        if (bookingDetails == null || bookingDetails.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        LocalDateTime earliestStart = bookingDetails.stream()
                .filter(this::isActiveDetail)
                .filter(detail -> detail.getPlayDate() != null)
                .filter(detail -> detail.getTimeSlot() != null && detail.getTimeSlot().getStartTime() != null)
                .map(detail -> LocalDateTime.of(detail.getPlayDate(), detail.getTimeSlot().getStartTime()))
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        if (Duration.between(LocalDateTime.now(), earliestStart).toMinutes() < 30) {
            throw new AppException(ErrorCode.CANCELLATION_WINDOW_EXPIRED);
        }
    }

    private boolean shouldMarkCompleted(List<BookingDetail> bookingDetails, LocalDateTime now) {
        if (bookingDetails == null || bookingDetails.isEmpty()) {
            return false;
        }

        LocalDateTime latestEnd = bookingDetails.stream()
                .filter(this::isActiveDetail)
                .map(this::resolveSlotEndTime)
                .filter(endTime -> endTime != null)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return latestEnd != null && !latestEnd.isAfter(now);
    }

    private boolean isActiveDetail(BookingDetail detail) {
        return detail != null
                && detail.getActiveKey() != null
                && detail.getActiveKey() == ACTIVE_KEY;
    }

    private LocalDateTime resolveSlotEndTime(BookingDetail detail) {
        if (detail == null
                || detail.getPlayDate() == null
                || detail.getTimeSlot() == null
                || detail.getTimeSlot().getEndTime() == null) {
            return null;
        }

        return LocalDateTime.of(detail.getPlayDate(), detail.getTimeSlot().getEndTime());
    }

    private record BookingCreationResult(List<BookingDetail> bookingDetails, BigDecimal totalAmount) {
    }
}
