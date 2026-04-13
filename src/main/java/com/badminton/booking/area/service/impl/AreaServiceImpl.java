package com.badminton.booking.area.service.impl;

import com.badminton.booking.area.dto.request.AreaRequest;
import com.badminton.booking.area.dto.response.AreaResponse;
import com.badminton.booking.home.repository.AreaRepository;
import com.badminton.booking.area.service.AreaService;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.domain.entity.Area;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("areaService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AreaServiceImpl implements AreaService {

    AreaRepository areaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> getAllAreas() {
        return areaRepository.findAllOrderByName().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AreaResponse getAreaById(Integer id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Khu vực"));
        return toResponse(area);
    }

    @Override
    @Transactional
    public AreaResponse createArea(AreaRequest request) {
        if (areaRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.CONFLICT, "Khu vực đã tồn tại");
        }

        Area area = Area.builder()
                .name(request.getName().trim())
                .build();

        Area saved = areaRepository.save(area);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AreaResponse updateArea(Integer id, AreaRequest request) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Khu vực"));

        if (areaRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new AppException(ErrorCode.CONFLICT, "Khu vực đã tồn tại");
        }

        area.setName(request.getName().trim());
        Area saved = areaRepository.save(area);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteArea(Integer id) {
        if (!areaRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Khu vực");
        }
        areaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return areaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameExcludingId(String name, Integer id) {
        return areaRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> findAllAreas() {
        return getAllAreas();
    }

    private AreaResponse toResponse(Area area) {
        return AreaResponse.builder()
                .id(area.getId())
                .name(area.getName())
                .branchCount(area.getBranchCount())
                .build();
    }
}
