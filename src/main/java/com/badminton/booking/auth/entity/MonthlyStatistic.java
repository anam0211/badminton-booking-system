package com.badminton.booking.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "monthly_statistics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_branch_month_year",
                        columnNames = {"branch_id", "month", "year"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "actual_revenue", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal actualRevenue = BigDecimal.ZERO;

    @Column(name = "expected_revenue", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal expectedRevenue = BigDecimal.ZERO;

    @Column(name = "total_bookings")
    @Builder.Default
    private Integer totalBookings = 0;

    @Column(name = "completed_bookings")
    @Builder.Default
    private Integer completedBookings = 0;

    @Column(name = "cancelled_bookings")
    @Builder.Default
    private Integer cancelledBookings = 0;

    @Column(name = "occupancy_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal occupancyRate = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}