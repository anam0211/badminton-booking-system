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
import java.time.LocalDate;
import java.time.LocalTime;
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
                total = total.add(price);
            }

            booking.setTotalAmount(total);
            booking = bookingRepository.save(booking);

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

    public List<BookingResponse> getBookingHistory(Long userId) {
        return mapToResponses(bookingRepository.findBookingHistoryByUserId(userId));
    }

    public List<BookingResponse> getAllBookings() {
        return mapToResponses(bookingRepository.findAllWithUserOrderByBookingDateDesc());
    }

    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return mapToResponse(booking);
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBooking_Id(bookingId);
        for (BookingDetail bookingDetail : bookingDetails) {
            bookingDetail.setActiveKey(INACTIVE_KEY);
        }
        bookingDetailRepository.saveAll(bookingDetails);

        return mapToResponse(booking);
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
            itemsByBookingId.computeIfAbsent(bookingId, ignored -> new java.util.ArrayList<>())
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
