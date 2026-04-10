package com.badminton.booking.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchListResponse {
    private Long branchId;
    private String branchName;
    private String branchImage;
    private String address;
    private BigDecimal minPrice;
    private Integer totalReviews;
    private Float avgRating;
}
