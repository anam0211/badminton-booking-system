package com.badminton.booking.home.service.impl;

import com.badminton.booking.dashboard.repository.BranchRepository;
import com.badminton.booking.home.dto.response.BranchListResponse;
import com.badminton.booking.home.service.BranchListService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchListServiceImpl implements BranchListService {

    BranchRepository branchRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BranchListResponse> getBranchList(String keyword, Integer districtId, BigDecimal minPrice, BigDecimal maxPrice, Integer rating, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<BranchRepository.BranchList> branchPage = branchRepository.getBranchList(keyword, districtId, minPrice, maxPrice, rating, pageable);

        return branchPage.map(list -> new BranchListResponse(
                list.getBranchId(),
                list.getBranchName(),
                list.getBranchImage(),
                list.getAddress(),
                list.getAreaName(),
                list.getMinPrice(),
                list.getTotalReviews(),
                list.getAvgRating()
        ));
    }
}