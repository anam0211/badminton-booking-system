package com.badminton.booking.dashboard.controller;

import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.dashboard.dto.request.*;
import com.badminton.booking.dashboard.repository.BranchRepository;
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
    BranchRepository branchRepository;

    @GetMapping
    public String redirectToDashboard(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails != null
                && customUserDetails.getUser() != null
                && customUserDetails.getUser().getRole() != null
                && RoleName.BRANCH_ADMIN.name().equalsIgnoreCase(customUserDetails.getUser().getRole().getName())) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/admin/branches";
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(value = "mode", defaultValue = "monthly") String mode,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "branchId", required = false) Long requestBranchId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Model model) {
        User currentAdmin = customUserDetails.getUser();
        String roleName = currentAdmin.getRole().getName();

        Long branchId = null;

        if (RoleName.BRANCH_ADMIN.name().equalsIgnoreCase(roleName)) {
            if (currentAdmin.getManagedBranch() == null) {
                model.addAttribute("errorMessage", "Tài khoản của bạn hiện chưa được phân công quản lý khu vực nào. Vui lòng liên hệ với người quản lý nhân sự để được thiết lập quyền.");
                return "error/403";
            }
            branchId = currentAdmin.getManagedBranch().getId();
        } else if (RoleName.ADMIN.name().equalsIgnoreCase(roleName)) {
            if (requestBranchId != null) {
                branchId = requestBranchId;
            } else {
                branchId = branchRepository.findAll().stream()
                        .filter(b -> !b.getIsDeleted())
                        .findFirst()
                        .map(com.badminton.booking.domain.entity.Branch::getId)
                        .orElse(null);
            }
        } else {
            model.addAttribute("errorMessage", "Bạn không có quyền truy cập trang này.");
            return "error/403";
        }

        if (branchId == null) {
            model.addAttribute("errorMessage", "Không tìm thấy chi nhánh nào trong hệ thống.");
            return "error/403";
        }

        // Khởi tạo giá trị mặc định nếu người dùng chưa chọn
        LocalDate now = LocalDate.now();
        int reqMonth = (month != null) ? month : now.getMonthValue();
        int reqYear = (year != null) ? year : now.getYear();

        // Truyền lại các tham số đang chọn để giữ trạng thái trên giao diện
        model.addAttribute("currentMode", mode);
        model.addAttribute("currentMonth", reqMonth);
        model.addAttribute("currentYear", reqYear);
        model.addAttribute("currentBranchId", branchId);

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
