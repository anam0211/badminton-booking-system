package com.example.court_booking.controller;

import com.example.court_booking.dto.request.BranchMonthlyDashboardRequest;
import com.example.court_booking.dto.request.BranchYearlyDashboardRequest;
import com.example.court_booking.dto.response.*;
import com.example.court_booking.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/statistic")
@CrossOrigin("*")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {
    DashboardService dashboardService;

    @GetMapping("/branches/dropdown")
    public ApiResponse<Map<Long, String>> getBranchDropdown(@RequestParam("areaId") Integer areaId){
        Map<Long, String> response = dashboardService.getBranchDropdown(areaId);
        return ApiResponse.<Map<Long, String>>builder()
                .data(response)
                .build();
    }
    @GetMapping("/revenue/monthly")
    public ApiResponse<BranchMonthlyRevenueResponse> getBranchRevenueByMonth(@ModelAttribute BranchMonthlyDashboardRequest request){
        BranchMonthlyRevenueResponse response = dashboardService.getBranchRevenueByMonth(request);
        return ApiResponse.<BranchMonthlyRevenueResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/revenue/yearly")
    public ApiResponse<BranchYearlyRevenueResponse> getBranchRevenueByYear(@ModelAttribute BranchYearlyDashboardRequest request){
        BranchYearlyRevenueResponse response = dashboardService.getBranchRevenueByYear(request);
        return ApiResponse.<BranchYearlyRevenueResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/overview/monthly")
    public ApiResponse<BranchOverviewResponse> getBranchOverviewByMonth(@ModelAttribute BranchMonthlyDashboardRequest request){
        BranchOverviewResponse response = dashboardService.getBranchOverviewByMonth(request);
        return ApiResponse.<BranchOverviewResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/overview/yearly")
    public ApiResponse<BranchOverviewResponse> getBranchOverviewByYear(@ModelAttribute BranchYearlyDashboardRequest request){
        BranchOverviewResponse response = dashboardService.getBranchOverviewByYear(request);
        return ApiResponse.<BranchOverviewResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/ranking/monthly")
    public ApiResponse<BranchCourtRankingResponse> getCourtRankingByMonth(@ModelAttribute BranchMonthlyDashboardRequest request){
        BranchCourtRankingResponse response = dashboardService.getCourtRankingByMonth(request);
        return ApiResponse.<BranchCourtRankingResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/ranking/yearly")
    public ApiResponse<BranchCourtRankingResponse> getCourtRankingByYear(@ModelAttribute BranchYearlyDashboardRequest request){
        BranchCourtRankingResponse response = dashboardService.getCourtRankingByYear(request);
        return ApiResponse.<BranchCourtRankingResponse>builder()
                .data(response)
                .build();
    }
}
