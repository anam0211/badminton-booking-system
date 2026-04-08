package com.badminton.booking.dashboard.service;

import com.badminton.booking.dashboard.dto.request.*;
import com.badminton.booking.dashboard.dto.response.*;

import java.util.Map;

public interface DashboardService {

    Map<Long, String> getBranchDropdown(Long managedBranchId, boolean isGlobalAdmin);

    BranchMonthlyRevenueResponse getBranchRevenueByMonth(BranchMonthlyDashboardRequest request);

    BranchYearlyRevenueResponse getBranchRevenueByYear(BranchYearlyDashboardRequest request);

    BranchOverviewResponse getBranchOverviewByMonth(BranchMonthlyDashboardRequest request);

    BranchOverviewResponse getBranchOverviewByYear(BranchYearlyDashboardRequest request);

    BranchCourtRankingResponse getCourtRankingByMonth(BranchMonthlyDashboardRequest request);

    BranchCourtRankingResponse getCourtRankingByYear(BranchYearlyDashboardRequest request);
}