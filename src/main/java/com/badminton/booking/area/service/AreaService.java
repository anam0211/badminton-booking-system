package com.badminton.booking.area.service;

import com.badminton.booking.area.dto.request.AreaRequest;
import com.badminton.booking.area.dto.response.AreaResponse;

import java.util.List;

public interface AreaService {

    List<AreaResponse> getAllAreas();

    List<AreaResponse> findAllAreas();

    AreaResponse getAreaById(Integer id);

    AreaResponse createArea(AreaRequest request);

    AreaResponse updateArea(Integer id, AreaRequest request);

    void deleteArea(Integer id);

    boolean existsByName(String name);

    boolean existsByNameExcludingId(String name, Integer id);
}
