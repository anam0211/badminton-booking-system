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
                false,
                "Lịch sử đặt sân",
                "Theo dõi các booking bạn đã tạo và lịch chơi đã chọn.",
                "Bạn chưa có booking nào.",
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
                true,
                "Danh sách booking",
                branchAdminView
                        ? "Bạn chỉ đang xem booking thuộc chi nhánh mình quản lý."
                        : "Admin có thể xem toàn bộ booking, thông tin khách hàng và lịch sân đã được đặt.",
                branchAdminView
                        ? "Chi nhánh bạn quản lý hiện chưa có booking nào."
                        : "Chưa có booking nào trong hệ thống.",
                bookings
        );
    }

    private List<Court> getCourtsByBranch(Long branchId) {
        if (branchId == null) {
            return Collections.emptyList();
        }
        return courtRepository.findByBranch_IdOrderByNameAsc(branchId);
    }

    private BookingListPageData buildListPageData(boolean adminView,
                                                  String pageTitle,
                                                  String pageDescription,
                                                  String emptyMessage,
                                                  List<BookingResponse> bookings) {
        BigDecimal totalAmount = bookings.stream()
                .filter(booking -> booking.getStatus() == null || !"CANCELLED".equalsIgnoreCase(booking.getStatus()))
                .map(BookingResponse::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int activeBookings = (int) bookings.stream()
                .filter(booking -> booking.getStatus() != null && !"CANCELLED".equalsIgnoreCase(booking.getStatus()))
                .count();

        int cancelledBookings = (int) bookings.stream()
                .filter(booking -> "CANCELLED".equalsIgnoreCase(booking.getStatus()))
                .count();

        return BookingListPageData.builder()
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
                .activeBookings(activeBookings)
                .cancelledBookings(cancelledBookings)
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
