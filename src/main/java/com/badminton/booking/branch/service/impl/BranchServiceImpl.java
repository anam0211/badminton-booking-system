package com.badminton.booking.branch.service.impl;

import com.badminton.booking.branch.dto.request.BranchRequest;
import com.badminton.booking.branch.dto.response.BranchListItem;
import com.badminton.booking.branch.dto.response.BranchResponse;
import com.badminton.booking.branch.repository.BranchAmenityRepository;
import com.badminton.booking.branch.repository.BranchImageRepository;
import com.badminton.booking.dashboard.repository.BranchRepository;
import com.badminton.booking.branch.service.BranchService;
import com.badminton.booking.common.enums.BranchStatus;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.domain.entity.Area;
import com.badminton.booking.domain.entity.Branch;
import com.badminton.booking.domain.entity.BranchAmenity;
import com.badminton.booking.domain.entity.BranchImage;
import com.badminton.booking.home.repository.AreaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchServiceImpl implements BranchService {

    BranchRepository branchRepository;
    AreaRepository areaRepository;
    BranchAmenityRepository branchAmenityRepository;
    BranchImageRepository branchImageRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BranchListItem> searchBranches(String keyword, Integer areaId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        BranchStatus branchStatus = (status != null && !status.trim().isEmpty()) ? BranchStatus.valueOf(status) : null;
        Page<Branch> branchPage = branchRepository.searchBranches(keyword, areaId, branchStatus, pageable);
        return branchPage.map(this::toListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chi nhánh"));
        return toResponse(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchDetail(Long id) {
        Branch branch = branchRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chi nhánh"));
        return toResponse(branch);
    }

    @Override
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        if (branchRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.CONFLICT, "Chi nhánh đã tồn tại");
        }

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Khu vực"));

        Branch branch = Branch.builder()
                .name(request.getName().trim())
                .address(request.getAddress().trim())
                .mapLink(request.getMapLink())
                .area(area)
                .status(BranchStatus.valueOf(request.getStatus() != null ? request.getStatus() : "OPEN"))
                .averageRating(0.0f)
                .totalReviews(0)
                .isDeleted(false)
                .build();

        Branch savedBranch = branchRepository.save(branch);

        if (request.getAmenityNames() != null && request.getAmenityNames().length > 0) {
            for (String amenityName : request.getAmenityNames()) {
                if (amenityName != null && !amenityName.trim().isEmpty()) {
                    BranchAmenity amenity = BranchAmenity.builder()
                            .branch(savedBranch)
                            .amenityName(amenityName.trim())
                            .build();
                    branchAmenityRepository.save(amenity);
                }
            }
        }

        if (request.getImageUrls() != null && request.getImageUrls().length > 0) {
            for (String imageUrl : request.getImageUrls()) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    BranchImage image = BranchImage.builder()
                            .branch(savedBranch)
                            .imageUrl(imageUrl.trim())
                            .build();
                    branchImageRepository.save(image);
                }
            }
        }

        return getBranchById(savedBranch.getId());
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch branch = branchRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chi nhánh"));

        if (branchRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new AppException(ErrorCode.CONFLICT, "Chi nhánh đã tồn tại");
        }

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Khu vực"));

        branch.setName(request.getName().trim());
        branch.setAddress(request.getAddress().trim());
        branch.setMapLink(request.getMapLink());
        branch.setArea(area);
        branch.setStatus(BranchStatus.valueOf(request.getStatus() != null ? request.getStatus() : "OPEN"));
        branchRepository.save(branch);

        if (request.getAmenityNames() != null) {
            branchAmenityRepository.deleteAllByBranchId(id);
            for (String amenityName : request.getAmenityNames()) {
                if (amenityName != null && !amenityName.trim().isEmpty()) {
                    BranchAmenity amenity = BranchAmenity.builder()
                            .branch(branch)
                            .amenityName(amenityName.trim())
                            .build();
                    branchAmenityRepository.save(amenity);
                }
            }
        }

        if (request.getImageUrls() != null) {
            branchImageRepository.deleteAllByBranchId(id);
            for (String imageUrl : request.getImageUrls()) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    BranchImage image = BranchImage.builder()
                            .branch(branch)
                            .imageUrl(imageUrl.trim())
                            .build();
                    branchImageRepository.save(image);
                }
            }
        }

        return getBranchById(id);
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chi nhánh"));
        branch.setIsDeleted(true);
        branchRepository.save(branch);
    }

    @Override
    @Transactional
    public void toggleBranchStatus(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chi nhánh"));

        BranchStatus currentStatus = branch.getStatus();
        if (currentStatus == BranchStatus.OPEN) {
            branch.setStatus(BranchStatus.CLOSED);
        } else {
            branch.setStatus(BranchStatus.OPEN);
        }
        branchRepository.save(branch);
    }

    private BranchListItem toListItem(Branch branch) {
        BigDecimal minPrice = branchRepository.findMinPriceByBranchId(branch.getId());
        Integer imageCount = branchImageRepository.countByBranchId(branch.getId());
        Integer amenityCount = branchAmenityRepository.countAmenitiesByBranchId(branch.getId());
        Integer courtCount = branchRepository.countCourtsByBranchId(branch.getId());

        return BranchListItem.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .areaName(branch.getArea() != null ? branch.getArea().getName() : "")
                .status(branch.getStatus().name())
                .averageRating(branch.getAverageRating())
                .totalReviews(branch.getTotalReviews())
                .imageCount(imageCount != null ? imageCount : 0)
                .amenityCount(amenityCount != null ? amenityCount : 0)
                .courtCount(courtCount != null ? courtCount : 0)
                .minPrice(minPrice)
                .build();
    }

    private BranchResponse toResponse(Branch branch) {
        List<String> amenities = branch.getBranchAmenities() != null
                ? branch.getBranchAmenities().stream()
                    .map(BranchAmenity::getAmenityName)
                    .collect(Collectors.toList())
                : List.of();

        List<String> images = branch.getBranchImages() != null
                ? branch.getBranchImages().stream()
                    .map(BranchImage::getImageUrl)
                    .collect(Collectors.toList())
                : List.of();

        BigDecimal minPrice = branchRepository.findMinPriceByBranchId(branch.getId());
        Integer courtCount = branchRepository.countCourtsByBranchId(branch.getId());

        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .mapLink(branch.getMapLink())
                .areaId(branch.getArea() != null ? branch.getArea().getId() : null)
                .areaName(branch.getArea() != null ? branch.getArea().getName() : "")
                .averageRating(branch.getAverageRating())
                .totalReviews(branch.getTotalReviews())
                .status(branch.getStatus())
                .amenities(amenities)
                .images(images)
                .minPrice(minPrice)
                .courtCount(courtCount != null ? courtCount : 0)
                .build();
    }
}
