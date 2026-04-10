package com.badminton.booking.home.service;

import com.badminton.booking.home.dto.response.BranchListResponse;

import java.util.List;

public interface HomeService {
    List<BranchListResponse> getFeatureBranches();
}
