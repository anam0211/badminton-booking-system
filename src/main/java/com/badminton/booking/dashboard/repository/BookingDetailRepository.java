package com.badminton.booking.dashboard.repository;

import com.badminton.booking.domain.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    @Query("""
                    SELECT SUM(bd.price) as monthlyRevenue FROM BookingDetail bd
                    JOIN bd.booking bk
                    JOIN bd.court c
                    WHERE c.branch.id = :branchId
                    AND bk.paymentStatus = 'PAID'
                    AND bk.status = 'COMPLETED'
                    AND bd.activeKey = 1
                    AND MONTH(bd.playDate) = :month
                    AND YEAR(bd.playDate) = :year
            """)
    BigDecimal sumRevenueByMonth(
            @Param("branchId") Long branchId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
                    SELECT DAY(bd.playDate) as day, 
                           SUM(bd.price) as totalRevenue
                    FROM BookingDetail bd
                    JOIN bd.booking bk
                    JOIN bd.court c
                    WHERE c.branch.id = :branchId
                    AND bk.paymentStatus = 'PAID'
                    AND bk.status = 'COMPLETED'
                    AND bd.activeKey = 1
                    AND MONTH(bd.playDate) = :month
                    AND YEAR(bd.playDate) = :year
                    GROUP BY DAY(bd.playDate)
            """)
    List<Object[]> sumRevenueByBranchAndDays(
            @Param("branchId") Long branchId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
                SELECT COUNT(bd.id) as totalBookings FROM BookingDetail bd
                JOIN bd.court c 
                JOIN bd.booking bk
                WHERE c.branch.id = :branchId
                AND MONTH(bd.playDate) = :month
                AND YEAR(bd.playDate) = :year   
            """)
    Long getTotalBookingsByMonth(
            @Param("branchId") Long branchId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
                SELECT COUNT(bd.id) as completedBookings FROM BookingDetail bd
                JOIN bd.court c 
                JOIN bd.booking bk
                WHERE c.branch.id = :branchId
                AND bk.status = 'COMPLETED'
                AND bd.activeKey = 1
                AND MONTH(bd.playDate) = :month
                AND YEAR(bd.playDate) = :year   
            """)
    Long getCompletedBookingsByMonth(
            @Param("branchId") Long branchId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
                SELECT COUNT(bd.id) as cancelledBookings FROM BookingDetail bd
                JOIN bd.court c 
                JOIN bd.booking bk
                WHERE c.branch.id = :branchId
                AND bk.status = 'CANCELLED'
                AND bd.activeKey IS NULL
                AND MONTH(bd.playDate) = :month
                AND YEAR(bd.playDate) = :year   
            """)
    Long getCancelledBookingsByMonth(
            @Param("branchId") Long branchId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
                    SELECT c.name as courtName, 
                           COUNT(bd.id) as totalBookings
                    FROM BookingDetail bd
                    JOIN bd.booking bk
                    JOIN bd.court c
                    WHERE c.branch.id = :branchId
                    AND bk.status IN ('COMPLETED', 'CONFIRMED')
                    AND bd.activeKey = 1
                    AND MONTH(bd.playDate) = :month
                    AND YEAR(bd.playDate) = :year
                    GROUP BY c.id, c.name
                    ORDER BY totalBookings DESC
            """)
    List<Object[]> getCourtRankingByMonth(
            @Param("branchId") Long branchId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
                    SELECT c.name as courtName, 
                           COUNT(bd.id) as totalBookings
                    FROM BookingDetail bd
                    JOIN bd.booking bk
                    JOIN bd.court c
                    WHERE c.branch.id = :branchId
                    AND bk.status IN ('COMPLETED', 'CONFIRMED')
                    AND bd.activeKey = 1
                    AND YEAR(bd.playDate) = :year
                    GROUP BY c.id, c.name
                    ORDER BY totalBookings DESC
            """)
    List<Object[]> getCourtRankingByYear(
            @Param("branchId") Long branchId,
            @Param("year") int year
    );
}
