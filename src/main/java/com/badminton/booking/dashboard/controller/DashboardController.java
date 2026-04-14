package com.badminton.booking.dashboard.controller;

import com.badminton.booking.dashboard.dto.request.*;
import com.badminton.booking.dashboard.service.DashboardService;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.security.CustomUserDetails;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {

    DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(value = "mode", defaultValue = "monthly") String mode,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Model model) {
        User currentAdmin = customUserDetails.getUser();

        if (currentAdmin.getManagedBranch() == null) {
            model.addAttribute("errorMessage", "Tài khoản của bạn hiện chưa được phân công quản lý khu vực nào. Vui lòng liên hệ với người quản lý nhân sự để được thiết lập quyền.");
            return "error/403";
        }

        Long branchId = currentAdmin.getManagedBranch().getId();

        // Khởi tạo giá trị mặc định nếu người dùng chưa chọn
        LocalDate now = LocalDate.now();
        int reqMonth = (month != null) ? month : now.getMonthValue();
        int reqYear = (year != null) ? year : now.getYear();

        // Truyền lại các tham số đang chọn để giữ trạng thái trên giao diện
        model.addAttribute("currentMode", mode);
        model.addAttribute("currentMonth", reqMonth);
        model.addAttribute("currentYear", reqYear);

        // Lấy dữ liệu Thống kê theo Mode (Tháng / Năm)
        if ("monthly".equals(mode)) {
            BranchMonthlyDashboardRequest req = new BranchMonthlyDashboardRequest(branchId, reqMonth, reqYear);

            model.addAttribute("overview", dashboardService.getBranchOverviewByMonth(req));
            model.addAttribute("revenueData", dashboardService.getBranchRevenueByMonth(req));
            model.addAttribute("rankingData", dashboardService.getCourtRankingByMonth(req));

        } else {
            BranchYearlyDashboardRequest req = new BranchYearlyDashboardRequest(branchId, reqYear);

            model.addAttribute("overview", dashboardService.getBranchOverviewByYear(req));
            model.addAttribute("revenueData", dashboardService.getBranchRevenueByYear(req));
            model.addAttribute("rankingData", dashboardService.getCourtRankingByYear(req));
        }

        // Trả về file HTML
        return "dashboard/dashboard";
    }
}