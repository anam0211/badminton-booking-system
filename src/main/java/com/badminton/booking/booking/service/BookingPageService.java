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
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.domain.entity.TimeSlot;
import com.badminton.booking.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    public BookingCreatePageData buildCreatePageData(Long branchId, LocalDate playDate) {
        List<com.badminton.booking.domain.entity.Branch> branches = branchRepository.findAll();
        List<Court> courts = getCourtsByBranch(branchId);
        List<TimeSlot> timeSlots = timeSlotRepository.findAllByOrderByStartTimeAsc();
        Map<Long, Map<Integer, SlotView>> slotGrid =
                bookingSlotGridService.buildSlotGrid(courts, timeSlots, playDate);

        return BookingCreatePageData.builder()
                .playDate(playDate)
                .selectedBranchName(resolveSelectedBranchName(branches, branchId))
                .branches(branches)
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
                "L\u1ecbch s\u1eed \u0111\u1eb7t s\u00e2n",
                "Theo d\u00f5i c\u00e1c booking b\u1ea1n \u0111\u00e3 t\u1ea1o, tr\u1ea1ng th\u00e1i thanh to\u00e1n v\u00e0 l\u1ecbch ch\u01a1i \u0111\u00e3 ch\u1ecdn.",
                "B\u1ea1n ch\u01b0a c\u00f3 booking n\u00e0o.",
                bookings
        );
    }

    public BookingListPageData buildAdminBookingListPageData(User viewer) {
        List<BookingResponse> bookings = bookingService.getAllBookingsForViewer(viewer);
        boolean branchAdminView = viewer != null
                && viewer.getRole() != null
                && viewer.getRole().getName() != null
                && RoleName.BRANCH_ADMIN.name().equalsIgnoreCase(viewer.getRole().getName());

        return buildListPageData(
                null,
                true,
                "Danh s\u00e1ch booking",
                branchAdminView
                        ? "B\u1ea1n ch\u1ec9 \u0111ang xem booking thu\u1ed9c chi nh\u00e1nh m\u00ecnh qu\u1ea3n l\u00fd."
                        : "Admin c\u00f3 th\u1ec3 xem to\u00e0n b\u1ed9 booking, th\u00f4ng tin kh\u00e1ch h\u00e0ng v\u00e0 l\u1ecbch s\u00e2n \u0111\u00e3 \u0111\u01b0\u1ee3c \u0111\u1eb7t.",
                branchAdminView
                        ? "Chi nh\u00e1nh b\u1ea1n qu\u1ea3n l\u00fd hi\u1ec7n ch\u01b0a c\u00f3 booking n\u00e0o."
                        : "Ch\u01b0a c\u00f3 booking n\u00e0o trong h\u1ec7 th\u1ed1ng.",
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
                .defaultBranchId(bookings.stream()
                        .map(BookingResponse::getBranchId)
                        .filter(java.util.Objects::nonNull)
                        .findFirst()
                        .orElse(null))
                .adminView(adminView)
                .pageTitle(pageTitle)
                .pageDescription(pageDescription)
                .emptyMessage(emptyMessage)
                .totalBookings(bookings.size())
                .totalAmount(totalAmount)
                .bookings(bookings)
                .build();
    }

    private String resolveSelectedBranchName(List<com.badminton.booking.domain.entity.Branch> branches, Long branchId) {
        if (branchId == null || branches == null || branches.isEmpty()) {
            return null;
        }

        return branches.stream()
                .filter(branch -> branchId.equals(branch.getId()))
                .map(com.badminton.booking.domain.entity.Branch::getName)
                .findFirst()
                .orElse(null);
    }
}
