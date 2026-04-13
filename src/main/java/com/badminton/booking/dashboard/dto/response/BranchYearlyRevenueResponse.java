package com.badminton.booking.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchYearlyRevenueResponse {
    private Long branchId;
    private int year;
    private List<MonthlyRevenue> monthlyBreakdown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyRevenue {
        private String monthAndYear;
        private BigDecimal revenue;
    }
}
