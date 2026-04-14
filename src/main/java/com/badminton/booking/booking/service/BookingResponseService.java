package com.badminton.booking.booking.service;

import com.badminton.booking.booking.dto.response.BookingDetailResponse;
import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.mapper.BookingDetailMapper;
import com.badminton.booking.booking.mapper.BookingMapper;
import com.badminton.booking.booking.repository.BookingDetailRepository;
import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.domain.entity.Booking;
import com.badminton.booking.domain.entity.BookingDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingResponseService {

    private static final byte ACTIVE_KEY = 1;

    private final BookingDetailRepository bookingDetailRepository;
    private final BookingMapper bookingMapper;
    private final BookingDetailMapper bookingDetailMapper;

    public BookingResponse toResponse(Booking booking) {
        return buildBookingResponse(booking, loadBookingItems(booking.getId()));
    }

    public List<BookingResponse> toResponses(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<BookingDetailResponse>> itemsByBookingId = loadBookingItemsByBookingIds(
                bookings.stream().map(Booking::getId).toList()
        );

        return bookings.stream()
                .map(booking -> buildBookingResponse(
                        booking,
                        itemsByBookingId.getOrDefault(booking.getId(), Collections.emptyList())
                ))
                .toList();
    }

    private List<BookingDetailResponse> loadBookingItems(Long bookingId) {
        return bookingDetailRepository.findDetailedByBookingId(bookingId)
                .stream()
                .map(bookingDetailMapper::toDto)
                .toList();
    }

    private Map<Long, List<BookingDetailResponse>> loadBookingItemsByBookingIds(List<Long> bookingIds) {
        Map<Long, List<BookingDetailResponse>> itemsByBookingId = new LinkedHashMap<>();

        for (BookingDetail bookingDetail : bookingDetailRepository.findDetailedByBookingIds(bookingIds)) {
            Long bookingId = bookingDetail.getBooking().getId();
            itemsByBookingId.computeIfAbsent(bookingId, ignored -> new ArrayList<>())
                    .add(bookingDetailMapper.toDto(bookingDetail));
        }

        return itemsByBookingId;
    }

    private BookingResponse buildBookingResponse(Booking booking, List<BookingDetailResponse> items) {
        BookingResponse response = bookingMapper.toDto(booking);
        response.setItems(items);
        response.setTotalSlots(items.size());

        applyBookingItemSummary(response, items);
        applyCancellationAvailability(response, booking, items);
        return response;
    }

    private void applyBookingItemSummary(BookingResponse response, List<BookingDetailResponse> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        response.setBranchId(items.get(0).getBranchId());
        response.setBranchName(items.get(0).getBranchName());
        response.setEarliestPlayDate(items.stream()
                .map(BookingDetailResponse::getPlayDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null));
        response.setLatestPlayDate(items.stream()
                .map(BookingDetailResponse::getPlayDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null));
    }

    private void applyCancellationAvailability(BookingResponse response,
                                               Booking booking,
                                               List<BookingDetailResponse> items) {
        CancellationAvailability availability = resolveCancellationAvailability(booking, items);
        response.setCancellable(availability.cancellable());
        response.setCancellationReason(availability.reason());
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

        LocalDateTime earliestStart = findEarliestActiveStart(items).orElse(null);
        if (earliestStart == null) {
            return new CancellationAvailability(false, "Booking không còn khung giờ hợp lệ để hủy.");
        }

        if (Duration.between(LocalDateTime.now(), earliestStart).toMinutes() < 30) {
            return new CancellationAvailability(false, "Chỉ được hủy sân trước 30 phút so với giờ bắt đầu.");
        }

        return new CancellationAvailability(true, null);
    }

    private java.util.Optional<LocalDateTime> findEarliestActiveStart(List<BookingDetailResponse> items) {
        return items.stream()
                .filter(this::isActiveItem)
                .filter(item -> item.getPlayDate() != null)
                .filter(item -> item.getStartTime() != null && !item.getStartTime().isBlank())
                .map(item -> LocalDateTime.of(item.getPlayDate(), LocalTime.parse(item.getStartTime())))
                .min(LocalDateTime::compareTo);
    }

    private boolean isActiveItem(BookingDetailResponse item) {
        return item != null && item.getActive() != null && item.getActive() == ACTIVE_KEY;
    }

    private record CancellationAvailability(boolean cancellable, String reason) {
    }
}
