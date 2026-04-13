package com.badminton.booking.home.service;

import com.badminton.booking.home.dto.response.BranchListResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface BranchListService {
    Page<BranchListResponse> getBranchList(String keyword, Integer districtId, BigDecimal minPrice, BigDecimal maxPrice, Integer rating, int page, int size);
}
