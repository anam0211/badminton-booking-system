package com.example.court_booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchOverviewResponse {
    private Long branchId;
    private BigDecimal totalRevenue;
    private BookingSummary bookingSummary;
    private ReviewSummary reviewSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingSummary {
        private Long totalBookings;
        private Long completedBookings;
        private Long cancelledBookings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewSummary {
        private Integer totalReviews;
        private Float avgRating;
    }
}
