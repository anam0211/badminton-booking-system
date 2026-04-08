package com.badminton.booking.dashboard.service.impl;

import com.badminton.booking.dashboard.dto.request.*;
import com.badminton.booking.dashboard.dto.response.*;
import com.badminton.booking.domain.entity.MonthlyStatistic;
import com.badminton.booking.dashboard.repository.BookingDetailRepository;
import com.badminton.booking.dashboard.repository.BranchRepository;
import com.badminton.booking.dashboard.repository.MonthlyStatisticRepository;
import com.badminton.booking.dashboard.repository.ReviewRepository;
import com.badminton.booking.dashboard.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardServiceImpl implements DashboardService {
    BookingDetailRepository bookingDetailRepository;
    MonthlyStatisticRepository monthlyStatisticRepository;
    ReviewRepository reviewRepository;
    BranchRepository branchRepository;

    @Override
    public Map<Long, String> getBranchDropdown(Long managedBranchId, boolean isGlobalAdmin) {
        List<Object[]> rawData = isGlobalAdmin
                ? branchRepository.getAllBranches()
                : branchRepository.getBranches(managedBranchId);
        return rawData.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));
    }

    //Thống kê doanh thu theo từng ngày trong tháng
    @Override
    public BranchMonthlyRevenueResponse getBranchRevenueByMonth(BranchMonthlyDashboardRequest request) {
        Long branchId = request.getBranchId();
        Integer month = request.getMonth();
        Integer year = request.getYear();
        LocalDate now = LocalDate.now();
        month = month != null ? month : now.getMonthValue();
        year = year != null ? year : now.getYear();

        List<Object[]> rawDailyRevenue = bookingDetailRepository.sumRevenueByBranchAndDays(branchId, month, year);
        Map<Integer, BigDecimal> dailyRevenueMap = rawDailyRevenue.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (BigDecimal) row[1]
                ));
        List<BranchMonthlyRevenueResponse.DailyRevenue> dailyRevenues = new ArrayList<>();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            BigDecimal revenue = dailyRevenueMap.getOrDefault(i, BigDecimal.ZERO);
            String date = String.format("%04d-%02d-%02d", year, month, i);
            dailyRevenues.add(new BranchMonthlyRevenueResponse.DailyRevenue(i, date, revenue));
        }

        return BranchMonthlyRevenueResponse.builder()
                .branchId(branchId)
                .period(BranchMonthlyRevenueResponse.Period.builder()
                        .month(month)
                        .year(year)
                        .build())
                .dailyBreakdown(dailyRevenues)
                .build();
    }

    //Thống kê doanh thu theo từng tháng trong năm
    @Override
    public BranchYearlyRevenueResponse getBranchRevenueByYear(BranchYearlyDashboardRequest request) {
        Long branchId = request.getBranchId();
        Integer requestYear = request.getYear();
        Integer currentYear = LocalDate.now().getYear();
        requestYear = requestYear != null ? requestYear : LocalDate.now().getYear();

        List<Object[]> rawMonthlyRevenue = monthlyStatisticRepository.getMonthlyRevenueUptoMonth(branchId, requestYear, 12);
        Map<Integer, BigDecimal> monthlyRevenueMap = rawMonthlyRevenue.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (BigDecimal) row[1]
                ));
        if (requestYear.equals(currentYear)) {
            Integer month = LocalDate.now().getMonthValue();
            BigDecimal revenue = bookingDetailRepository.sumRevenueByMonth(branchId, month, requestYear);
            revenue = revenue != null ? revenue : BigDecimal.ZERO;
            monthlyRevenueMap.put(month, revenue);
        }
        List<BranchYearlyRevenueResponse.MonthlyRevenue> monthlyRevenues = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            BigDecimal revenue = monthlyRevenueMap.getOrDefault(i, BigDecimal.ZERO);
            String monthAndYear = String.format("%04d-%02d", requestYear, i);
            monthlyRevenues.add(new BranchYearlyRevenueResponse.MonthlyRevenue(monthAndYear, revenue));
        }

        return BranchYearlyRevenueResponse.builder()
                .branchId(branchId)
                .year(requestYear)
                .monthlyBreakdown(monthlyRevenues)
                .build();
    }

    //Dữ liệu tổng hợp theo tháng
    @Override
    public BranchOverviewResponse getBranchOverviewByMonth(BranchMonthlyDashboardRequest request) {
        Long branchId = request.getBranchId();
        Integer month = request.getMonth();
        Integer year = request.getYear();
        LocalDate now = LocalDate.now();
        month = month != null ? month : now.getMonthValue();
        year = year != null ? year : now.getYear();

        YearMonth currentPeriod = YearMonth.now();
        YearMonth requestPeriod = YearMonth.of(year, month);
        ReviewRepository.ReviewSummary reviewSummary = reviewRepository.getReviewSummaryByMonth(branchId, year, month);

        if (requestPeriod.equals(currentPeriod) || requestPeriod.isAfter(currentPeriod)) {
            return realtimeOverviewByMonth(branchId, year, month, reviewSummary);
        } else {
            Optional<MonthlyStatistic> snapshotOpt = monthlyStatisticRepository
                    .findByBranchIdAndYearAndMonth(branchId, year, month);
            if (snapshotOpt.isPresent()) {
                return snapshotToResponse(snapshotOpt.get(), reviewSummary);
            } else {
                return realtimeOverviewByMonth(branchId, year, month, reviewSummary);
            }
        }
    }

    //Dữ liệu tổng hợp theo năm
    @Override
    public BranchOverviewResponse getBranchOverviewByYear(BranchYearlyDashboardRequest request) {
        Long branchId = request.getBranchId();
        Integer requestYear = request.getYear();
        Integer currentYear = LocalDate.now().getYear();
        requestYear = requestYear != null ? requestYear : LocalDate.now().getYear();

        ReviewRepository.ReviewSummary reviewSummary = reviewRepository.getReviewSummaryByYear(branchId, requestYear);

        BigDecimal revenue = BigDecimal.ZERO;

        Long totalBookings = 0L;
        Long completedBookings = 0L;
        Long cancelledBookings = 0L;

        if (requestYear < currentYear) {
            MonthlyStatisticRepository.YearlyOverviewSummary snapshot = monthlyStatisticRepository.getYearlyOverviewUpToMonth(branchId, requestYear, 12);
            if (snapshot != null) {
                revenue = snapshot.getTotalRevenue();
                revenue = revenue != null ? revenue : BigDecimal.ZERO;

                totalBookings = snapshot.getTotalBookings() != null ? snapshot.getTotalBookings() : 0L;
                completedBookings = snapshot.getCompletedBookings() != null ? snapshot.getCompletedBookings() : 0L;
                cancelledBookings = snapshot.getCancelledBookings() != null ? snapshot.getCancelledBookings() : 0L;
            }
        } else if (requestYear.equals(currentYear)) {
            Integer currentMonth = LocalDate.now().getMonthValue();
            if (currentMonth > 1) {
                MonthlyStatisticRepository.YearlyOverviewSummary snapshot = monthlyStatisticRepository.getYearlyOverviewUpToMonth(branchId, requestYear, currentMonth - 1);
                if (snapshot != null) {
                    revenue = snapshot.getTotalRevenue();
                    revenue = revenue != null ? revenue : BigDecimal.ZERO;

                    totalBookings = snapshot.getTotalBookings() != null ? snapshot.getTotalBookings() : 0L;
                    completedBookings = snapshot.getCompletedBookings() != null ? snapshot.getCompletedBookings() : 0L;
                    cancelledBookings = snapshot.getCancelledBookings() != null ? snapshot.getCancelledBookings() : 0L;
                }
            }
            BigDecimal currentRevenue = bookingDetailRepository.sumRevenueByMonth(branchId, currentMonth, currentYear);
            currentRevenue = currentRevenue != null ? currentRevenue : BigDecimal.ZERO;

            Long currentTotalBookings = bookingDetailRepository.getTotalBookingsByMonth(branchId, currentMonth, currentYear);
            Long currentCompletedBookings = bookingDetailRepository.getCompletedBookingsByMonth(branchId, currentMonth, currentYear);
            Long currentCancelledBookings = bookingDetailRepository.getCancelledBookingsByMonth(branchId, currentMonth, currentYear);

            revenue = revenue.add(currentRevenue != null ? currentRevenue : BigDecimal.ZERO);

            totalBookings += (currentTotalBookings != null ? currentTotalBookings : 0L);
            completedBookings += (currentCompletedBookings != null ? currentCompletedBookings : 0L);
            cancelledBookings += (currentCancelledBookings != null ? currentCancelledBookings : 0L);
        }
        Integer totalReviews = reviewSummary.getTotalReviews();
        Float avgRating = reviewSummary.getAvgRating();
        avgRating = avgRating == null ? 0f : Math.round(avgRating * 100) / 100f;

        return BranchOverviewResponse.builder()
                .branchId(branchId)
                .totalRevenue(revenue)
                .bookingSummary(BranchOverviewResponse.BookingSummary.builder()
                        .totalBookings(totalBookings)
                        .completedBookings(completedBookings)
                        .cancelledBookings(cancelledBookings)
                        .build())
                .reviewSummary(BranchOverviewResponse.ReviewSummary.builder()
                        .totalReviews(totalReviews)
                        .avgRating(avgRating)
                        .build())
                .build();
    }

    @Override
    public BranchCourtRankingResponse getCourtRankingByMonth(BranchMonthlyDashboardRequest request) {
        Long branchId = request.getBranchId();
        Integer month = request.getMonth();
        Integer year = request.getYear();
        LocalDate now = LocalDate.now();
        month = month != null ? month : now.getMonthValue();
        year = year != null ? year : now.getYear();

        List<Object[]> rawData = bookingDetailRepository.getCourtRankingByMonth(branchId, month, year);
        List<BranchCourtRankingResponse.CourtRanking> courtRankings = new ArrayList<>();

        for (Object[] data : rawData) {
            String courtName = (String) data[0];
            Long totalBookings = (Long) data[1];
            courtRankings.add(new BranchCourtRankingResponse.CourtRanking(courtName, totalBookings));
        }

        return BranchCourtRankingResponse.builder()
                .branchId(branchId)
                .courtRanking(courtRankings)
                .build();
    }

    @Override
    public BranchCourtRankingResponse getCourtRankingByYear(BranchYearlyDashboardRequest request) {
        Long branchId = request.getBranchId();
        Integer year = request.getYear();
        LocalDate now = LocalDate.now();
        year = year != null ? year : now.getYear();

        List<Object[]> rawData = bookingDetailRepository.getCourtRankingByYear(branchId, year);
        List<BranchCourtRankingResponse.CourtRanking> courtRankings = new ArrayList<>();

        for (Object[] data : rawData) {
            String courtName = (String) data[0];
            Long totalBookings = (Long) data[1];
            courtRankings.add(new BranchCourtRankingResponse.CourtRanking(courtName, totalBookings));
        }

        return BranchCourtRankingResponse.builder()
                .branchId(branchId)
                .courtRanking(courtRankings)
                .build();
    }

    private BranchOverviewResponse realtimeOverviewByMonth(Long branchId, int year, int month, ReviewRepository.ReviewSummary reviewSummary) {

        BigDecimal revenue = bookingDetailRepository.sumRevenueByMonth(branchId, month, year);
        revenue = revenue != null ? revenue : BigDecimal.ZERO;

        Long totalBookings = bookingDetailRepository.getTotalBookingsByMonth(branchId, month, year);
        Long completedBookings = bookingDetailRepository.getCompletedBookingsByMonth(branchId, month, year);
        Long cancelledBookings = bookingDetailRepository.getCancelledBookingsByMonth(branchId, month, year);

        Integer totalReviews = reviewSummary.getTotalReviews();
        Float avgRating = reviewSummary.getAvgRating();
        avgRating = avgRating == null ? 0f : Math.round(avgRating * 100) / 100f;

        return BranchOverviewResponse.builder()
                .branchId(branchId)
                .totalRevenue(revenue)
                .bookingSummary(BranchOverviewResponse.BookingSummary.builder()
                        .totalBookings(totalBookings)
                        .completedBookings(completedBookings)
                        .cancelledBookings(cancelledBookings)
                        .build())
                .reviewSummary(BranchOverviewResponse.ReviewSummary.builder()
                        .totalReviews(totalReviews)
                        .avgRating(avgRating)
                        .build())
                .build();
    }

    private BranchOverviewResponse snapshotToResponse(MonthlyStatistic snapshot, ReviewRepository.ReviewSummary reviewSummary) {
        Long branchId = snapshot.getBranch().getId();

        BigDecimal revenue = snapshot.getTotalRevenue();

        Long totalBookings = snapshot.getTotalBookings();
        Long completedBookings = snapshot.getCompletedBookings();
        Long cancelledBookings = snapshot.getCancelledBookings();

        Integer totalReviews = reviewSummary.getTotalReviews();
        Float avgRating = reviewSummary.getAvgRating();
        avgRating = avgRating == null ? 0f : Math.round(avgRating * 100) / 100f;

        return BranchOverviewResponse.builder()
                .branchId(branchId)
                .totalRevenue(revenue)
                .bookingSummary(BranchOverviewResponse.BookingSummary.builder()
                        .totalBookings(totalBookings)
                        .completedBookings(completedBookings)
                        .cancelledBookings(cancelledBookings)
                        .build())
                .reviewSummary(BranchOverviewResponse.ReviewSummary.builder()
                        .totalReviews(totalReviews)
                        .avgRating(avgRating)
                        .build())
                .build();
    }
}
