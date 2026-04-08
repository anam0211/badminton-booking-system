package com.badminton.booking.booking.service;

import com.badminton.booking.booking.dto.response.BookingCreatePageData;
import com.badminton.booking.booking.dto.response.BookingListPageData;
import com.badminton.booking.booking.dto.response.BookingPageMode;
import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.dto.response.BookingResultPageData;
import com.badminton.booking.booking.dto.response.SlotView;
import com.badminton.booking.booking.repository.BranchRepository;
import com.badminton.booking.booking.repository.CourtRepository;
import com.badminton.booking.booking.repository.TimeSlotRepository;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.domain.entity.TimeSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingPageService {

    private final BranchRepository branchRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingSlotGridService bookingSlotGridService;
    private final BookingService bookingService;

    public BookingCreatePageData buildCreatePageData(Long userId, Long branchId, LocalDate playDate) {
        List<Court> courts = getCourtsByBranch(branchId);
        List<TimeSlot> timeSlots = timeSlotRepository.findAllByOrderByStartTimeAsc();
        Map<Long, Map<Integer, SlotView>> slotGrid =
                bookingSlotGridService.buildSlotGrid(courts, timeSlots, playDate);

        return BookingCreatePageData.builder()
                .userId(userId)
                .playDate(playDate)
                .branches(branchRepository.findAll())
                .courts(courts)
                .timeSlots(timeSlots)
                .slotGrid(slotGrid)
                .build();
    }

    public BookingResultPageData buildResultPageData(BookingResponse booking, BookingPageMode pageMode) {
        return BookingResultPageData.builder()
                .booking(booking)
                .pageMode(pageMode)
                .build();
    }

    public BookingListPageData buildHistoryPageData(Long userId) {
        List<BookingResponse> bookings = bookingService.getBookingHistory(userId);
        return buildListPageData(
                userId,
                false,
                "Lich su dat san",
                "Theo doi cac booking ma ban da tao, trang thai thanh toan va lich choi da chon.",
                "Ban chua co booking nao.",
                bookings
        );
    }

    public BookingListPageData buildAdminBookingListPageData() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return buildListPageData(
                null,
                true,
                "Danh sach booking",
                "Admin co the xem toan bo booking, thong tin khach hang va lich san da duoc dat.",
                "Chua co booking nao trong he thong.",
                bookings
        );
    }

    private List<Court> getCourtsByBranch(Long branchId) {
        if (branchId == null) {
            return Collections.emptyList();
        }
        return courtRepository.findByBranch_IdOrderByNameAsc(branchId);
    }

    private BookingListPageData buildListPageData(Long userId,
                                                  boolean adminView,
                                                  String pageTitle,
                                                  String pageDescription,
                                                  String emptyMessage,
                                                  List<BookingResponse> bookings) {
        BigDecimal totalAmount = bookings.stream()
                .map(BookingResponse::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BookingListPageData.builder()
                .userId(userId)
                .adminView(adminView)
                .pageTitle(pageTitle)
                .pageDescription(pageDescription)
                .emptyMessage(emptyMessage)
                .totalBookings(bookings.size())
                .totalAmount(totalAmount)
                .bookings(bookings)
                .build();
    }
}
