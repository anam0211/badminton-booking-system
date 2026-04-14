package com.badminton.booking.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monthly_statistics", uniqueConstraints = {@UniqueConstraint(
        name = "uk_branch_month_year",
        columnNames = {"branch_id", "month", "year"}
)})
public class MonthlyStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull
    @Column(nullable = false)
    private Integer month;

    @NotNull
    @Column(nullable = false)
    private Integer year;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Builder.Default
    private Long totalBookings = 0L;

    @Builder.Default
    private Long completedBookings = 0L;

    @Builder.Default
    private Long cancelledBookings = 0L;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}