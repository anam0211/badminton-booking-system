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
public class BranchMonthlyRevenueResponse {
    private Long branchId;
    private Period period;
    private List<DailyRevenue> dailyBreakdown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Period {
        private Integer month;
        private Integer year;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyRevenue {
        private Integer day;
        private String date;
        private BigDecimal revenue;
    }
}