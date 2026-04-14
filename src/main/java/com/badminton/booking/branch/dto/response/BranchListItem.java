package com.badminton.booking.branch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchListItem {
    private Long id;
    private String name;
    private String address;
    private String areaName;
    private String status;
    private Float averageRating;
    private Integer totalReviews;
    private Integer imageCount;
    private Integer amenityCount;
    private Integer courtCount;
    private BigDecimal minPrice;
}
