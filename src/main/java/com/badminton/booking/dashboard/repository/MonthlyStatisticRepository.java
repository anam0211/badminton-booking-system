package com.badminton.booking.dashboard.repository;

import com.badminton.booking.domain.entity.MonthlyStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyStatisticRepository extends JpaRepository<MonthlyStatistic, Long> {
    interface YearlyOverviewSummary {
        BigDecimal getTotalRevenue();

        Long getTotalBookings();

        Long getCompletedBookings();

        Long getCancelledBookings();
    }

    Optional<MonthlyStatistic> findByBranchIdAndYearAndMonth(Long branchId, int year, int month);

    @Query("""
                SELECT 
                    SUM(ms.totalRevenue) as totalRevenue,
                    SUM(ms.totalBookings) as totalBookings,
                    SUM(ms.completedBookings) as completedBookings,
                    SUM(ms.cancelledBookings) as cancelledBookings
                FROM MonthlyStatistic ms
                WHERE ms.branch.id = :branchId
                AND ms.year = :year
                AND ms.month <= :month
            """)
    YearlyOverviewSummary getYearlyOverviewUpToMonth(
            @Param("branchId") Long branchId,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("""
                SELECT
                    ms.month, 
                    ms.totalRevenue
                FROM MonthlyStatistic ms
                WHERE ms.branch.id = :branchId
                AND ms.month <= :month 
                AND ms.year = :year
                ORDER BY ms.month ASC
            """)
    List<Object[]> getMonthlyRevenueUptoMonth(
            @Param("branchId") Long branchId,
            @Param("year") int year,
            @Param("month") int month
    );
}
