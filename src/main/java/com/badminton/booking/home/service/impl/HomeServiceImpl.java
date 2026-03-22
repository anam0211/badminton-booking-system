package com.badminton.booking.home.service.impl;

import com.badminton.booking.home.dto.response.HomeResponse;
import com.badminton.booking.dashboard.repository.BranchRepository;
import com.badminton.booking.home.service.HomeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomeServiceImpl implements HomeService {
    BranchRepository branchRepository;

    @Override
    public List<HomeResponse> getBranchList(){
        List<BranchRepository.BranchList> branchList = branchRepository.getBranchList();
        List<HomeResponse> response = new ArrayList<>();
        for(BranchRepository.BranchList list : branchList){
            Long branchId = list.getBranchId();
            String branchName = list.getBranchName();
            String branchImage = list.getBranchImage();
            String address = list.getAddress();
            BigDecimal minPrice = list.getMinPrice();
            Integer totalReviews = list.getTotalReviews();
            Float avgRating = list.getAvgRating();
            response.add(new HomeResponse(branchId, branchName, branchImage, address, minPrice, totalReviews, avgRating));
        }
        return response;
    }
}
