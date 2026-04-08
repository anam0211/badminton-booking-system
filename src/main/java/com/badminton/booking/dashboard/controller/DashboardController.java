package com.badminton.booking.dashboard.controller;

import com.badminton.booking.common.enums.RoleName;
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
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {

    DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "mode", defaultValue = "monthly") String mode,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Model model) {
        if (customUserDetails == null) {
            return "redirect:/login";
        }

        User currentAdmin = customUserDetails.getUser();
        boolean isGlobalAdmin = RoleName.ADMIN.name().equalsIgnoreCase(currentAdmin.getRole().getName());

        if (!isGlobalAdmin && currentAdmin.getManagedBranch() == null) {
            model.addAttribute("errorMessage", "Tài khoản của bạn hiện chưa được phân công quản lý cơ sở nào. Vui lòng liên hệ quản trị viên để được thiết lập quyền.");
            return "error/403";
        }

        Long managedBranchId = isGlobalAdmin ? null : currentAdmin.getManagedBranch().getId();

        // Khởi tạo giá trị mặc định nếu người dùng chưa chọn
        LocalDate now = LocalDate.now();
        int reqMonth = (month != null) ? month : now.getMonthValue();
        int reqYear = (year != null) ? year : now.getYear();

        // Lấy danh sách cơ sở đổ vào Dropdown
        Map<Long, String> branches = dashboardService.getBranchDropdown(managedBranchId, isGlobalAdmin);
        model.addAttribute("branches", branches);

        Long actualBranchId = isGlobalAdmin ? branchId : managedBranchId;
        if(branches.isEmpty()){
            actualBranchId = -1L;
        }
        else if (actualBranchId == null || !branches.containsKey(actualBranchId)) {
            actualBranchId = branches.keySet().iterator().next();
        }

        // Truyền lại các tham số đang chọn để giữ trạng thái trên giao diện
        model.addAttribute("currentBranchId", actualBranchId);
        model.addAttribute("currentMode", mode);
        model.addAttribute("currentMonth", reqMonth);
        model.addAttribute("currentYear", reqYear);

        // Lấy dữ liệu Thống kê theo Mode (Tháng / Năm)
        if ("monthly".equals(mode)) {
            BranchMonthlyDashboardRequest req = new BranchMonthlyDashboardRequest(actualBranchId, reqMonth, reqYear);

            model.addAttribute("overview", dashboardService.getBranchOverviewByMonth(req));
            model.addAttribute("revenueData", dashboardService.getBranchRevenueByMonth(req));
            model.addAttribute("rankingData", dashboardService.getCourtRankingByMonth(req));

        } else {
            BranchYearlyDashboardRequest req = new BranchYearlyDashboardRequest(actualBranchId, reqYear);

            model.addAttribute("overview", dashboardService.getBranchOverviewByYear(req));
            model.addAttribute("revenueData", dashboardService.getBranchRevenueByYear(req));
            model.addAttribute("rankingData", dashboardService.getCourtRankingByYear(req));
        }

        // Trả về file HTML
        return "dashboard/dashboard";
    }
}