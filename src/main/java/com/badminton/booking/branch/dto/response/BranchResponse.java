package com.badminton.booking.branch.dto.response;

import com.badminton.booking.common.enums.BranchStatus;
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
public class BranchResponse {
    private Long id;
    private String name;
    private String address;
    private String mapLink;
    private Integer areaId;
    private String areaName;
    private String description;
    private Float averageRating;
    private Integer totalReviews;
    private BranchStatus status;
    private List<String> amenities;
    private List<String> images;
    private BigDecimal minPrice;
    private Integer courtCount;
}
