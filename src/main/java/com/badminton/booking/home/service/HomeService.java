package com.badminton.booking.home.service;

import com.badminton.booking.home.dto.response.HomeResponse;

import java.util.List;

public interface HomeService {
    List<HomeResponse> getFeatureBranches();
}
