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
import com.badminton.booking.domain.entity.Branch;
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

    private static final String CANCELLED_STATUS = "CANCELLED";

    private final BranchRepository branchRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingSlotGridService bookingSlotGridService;
    private final BookingService bookingService;

    public BookingCreatePageData buildCreatePageData(Long branchId, LocalDate playDate) {
        List<Branch> branches = branchRepository.findAll();
        List<Court> courts = getCourtsByBranch(branchId);
        List<TimeSlot> timeSlots = timeSlotRepository.findAllByOrderByStartTimeAsc();
        Map<Long, Map<Integer, SlotView>> slotGrid = bookingSlotGridService.buildSlotGrid(courts, timeSlots, playDate);

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
        return buildListPageData(
                bookingService.getBookingHistory(userId),
                false,
                "Lịch sử đặt sân",
                "Theo dõi các booking bạn đã tạo và lịch chơi đã chọn.",
                "Bạn chưa có booking nào."
        );
    }

    public BookingListPageData buildAdminBookingListPageData(User viewer) {
        boolean branchAdminView = hasRole(viewer, RoleName.BRANCH_ADMIN);

        return buildListPageData(
                bookingService.getAllBookingsForViewer(viewer),
                true,
                "Danh sách booking",
                branchAdminView
                        ? "Bạn chỉ đang xem booking thuộc chi nhánh mình quản lý."
                        : "Admin có thể xem toàn bộ booking, thông tin khách hàng và lịch sân đã được đặt.",
                branchAdminView
                        ? "Chi nhánh bạn quản lý hiện chưa có booking nào."
                        : "Chưa có booking nào trong hệ thống."
        );
    }

    private BookingListPageData buildListPageData(List<BookingResponse> bookings,
                                                  boolean adminView,
                                                  String pageTitle,
                                                  String pageDescription,
                                                  String emptyMessage) {
        BookingListSummary summary = summarizeBookings(bookings);

        return BookingListPageData.builder()
                .defaultBranchId(summary.defaultBranchId())
                .adminView(adminView)
                .pageTitle(pageTitle)
                .pageDescription(pageDescription)
                .emptyMessage(emptyMessage)
                .totalBookings(bookings.size())
                .totalAmount(summary.totalAmount())
                .activeBookings(summary.activeBookings())
                .cancelledBookings(summary.cancelledBookings())
                .bookings(bookings)
                .build();
    }

    private BookingListSummary summarizeBookings(List<BookingResponse> bookings) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int activeBookings = 0;
        int cancelledBookings = 0;
        Long defaultBranchId = null;

        for (BookingResponse booking : bookings) {
            if (booking == null) {
                continue;
            }

            if (defaultBranchId == null && booking.getBranchId() != null) {
                defaultBranchId = booking.getBranchId();
            }

            if (isCancelled(booking)) {
                cancelledBookings++;
                continue;
            }

            activeBookings++;
            if (booking.getTotalAmount() != null) {
                totalAmount = totalAmount.add(booking.getTotalAmount());
            }
        }

        return new BookingListSummary(totalAmount, activeBookings, cancelledBookings, defaultBranchId);
    }

    private List<Court> getCourtsByBranch(Long branchId) {
        if (branchId == null) {
            return Collections.emptyList();
        }
        return courtRepository.findByBranch_IdOrderByNameAsc(branchId);
    }

    private String resolveSelectedBranchName(List<Branch> branches, Long branchId) {
        if (branchId == null || branches == null || branches.isEmpty()) {
            return null;
        }

        return branches.stream()
                .filter(branch -> branchId.equals(branch.getId()))
                .map(Branch::getName)
                .findFirst()
                .orElse(null);
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user != null
                && user.getRole() != null
                && user.getRole().getName() != null
                && roleName.name().equalsIgnoreCase(user.getRole().getName());
    }

    private boolean isCancelled(BookingResponse booking) {
        return booking != null
                && booking.getStatus() != null
                && CANCELLED_STATUS.equalsIgnoreCase(booking.getStatus());
    }

    private record BookingListSummary(BigDecimal totalAmount,
                                      int activeBookings,
                                      int cancelledBookings,
                                      Long defaultBranchId) {
    }
}
