package com.badminton.booking.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchCourtRankingResponse {
    private Long branchId;
    private List<CourtRanking> courtRanking;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourtRanking {
        private String courtName;
        private Long totalBookings;
    }
}
