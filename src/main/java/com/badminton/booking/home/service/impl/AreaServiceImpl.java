package com.badminton.booking.home.service.impl;

import com.badminton.booking.domain.entity.Area;
import com.badminton.booking.home.repository.AreaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("areaService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AreaServiceImpl {
    AreaRepository areaRepository;

    @Cacheable("areas")
    public List<Area> findAllAreas() {
        return areaRepository.findAll();
    }
}
