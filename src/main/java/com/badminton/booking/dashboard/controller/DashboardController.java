package com.badminton.booking.dashboard.controller;

import com.badminton.booking.dashboard.dto.request.*;
import com.badminton.booking.dashboard.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
            @RequestParam(value = "areaId", defaultValue = "1") Integer areaId,
            @RequestParam(value = "mode", defaultValue = "monthly") String mode,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            Model model) {

        // 1. Khởi tạo giá trị mặc định nếu người dùng chưa chọn
        LocalDate now = LocalDate.now();
        int reqMonth = (month != null) ? month : now.getMonthValue();
        int reqYear = (year != null) ? year : now.getYear();

        // 2. Lấy danh sách cơ sở đổ vào Dropdown
        Map<Long, String> branches = dashboardService.getBranchDropdown(areaId);
        model.addAttribute("branches", branches);

        Long actualBranchId = branchId;
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

        // 3. Lấy dữ liệu Thống kê theo Mode (Tháng / Năm)
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

        // 4. Trả về file HTML (thư mục src/main/resources/templates/dashboard/dashboard.html)
        return "dashboard/dashboard";
    }
}