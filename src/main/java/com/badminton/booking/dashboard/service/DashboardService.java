package com.example.court_booking.service;

import com.example.court_booking.dto.request.BranchMonthlyDashboardRequest;
import com.example.court_booking.dto.request.BranchYearlyDashboardRequest;
import com.example.court_booking.dto.response.*;

import java.util.Map;

public interface DashboardService {

    Map<Long, String> getBranchDropdown(Integer areaId);

    BranchMonthlyRevenueResponse getBranchRevenueByMonth(BranchMonthlyDashboardRequest request);

    BranchYearlyRevenueResponse getBranchRevenueByYear(BranchYearlyDashboardRequest request);

    BranchOverviewResponse getBranchOverviewByMonth(BranchMonthlyDashboardRequest request);

    BranchOverviewResponse getBranchOverviewByYear(BranchYearlyDashboardRequest request);

    BranchCourtRankingResponse getCourtRankingByMonth(BranchMonthlyDashboardRequest request);

    BranchCourtRankingResponse getCourtRankingByYear(BranchYearlyDashboardRequest request);
}