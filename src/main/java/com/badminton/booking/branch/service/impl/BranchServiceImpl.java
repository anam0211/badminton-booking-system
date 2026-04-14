package com.badminton.booking.branch.service.impl;

import com.badminton.booking.branch.dto.request.BranchRequest;
import com.badminton.booking.branch.dto.response.BranchListItem;
import com.badminton.booking.branch.dto.response.BranchResponse;
import com.badminton.booking.branch.repository.BranchAmenityRepository;
import com.badminton.booking.branch.repository.BranchImageRepository;
import com.badminton.booking.branch.service.BranchSecurityService;
import com.badminton.booking.branch.service.BranchService;
import com.badminton.booking.common.enums.BranchStatus;
import com.badminton.booking.common.exception.AccessDeniedCustomException;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.common.service.ImageStorageService;
import com.badminton.booking.dashboard.repository.BranchRepository;
import com.badminton.booking.domain.entity.Area;
import com.badminton.booking.domain.entity.Branch;
import com.badminton.booking.domain.entity.BranchAmenity;
import com.badminton.booking.domain.entity.BranchImage;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.domain.repository.UserRepository;
import com.badminton.booking.home.repository.AreaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchServiceImpl implements BranchService {

    BranchRepository branchRepository;
    AreaRepository areaRepository;
    BranchAmenityRepository branchAmenityRepository;
    BranchImageRepository branchImageRepository;
    BranchSecurityService branchSecurityService;
    ImageStorageService imageStorageService;
    UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BranchListItem> searchBranches(String keyword, Integer areaId, String status, int page, int size) {
        var managedBranch = branchSecurityService.getManagedBranchForCurrentUser();
        if (managedBranch.isPresent()) {
            return new PageImpl<>(List.of(toListItem(managedBranch.get())), PageRequest.of(page, size), 1);
        }

        Pageable pageable = PageRequest.of(page, size);
        BranchStatus branchStatus = (status != null && !status.trim().isEmpty()) ? BranchStatus.valueOf(status) : null;
        return branchRepository.searchBranches(keyword, areaId, branchStatus, pageable).map(this::toListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chi nhánh"));
        branchSecurityService.ensureUserCanManageBranch(id);
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
        if (branchSecurityService.getManagedBranchForCurrentUser().isPresent()) {
            throw new AccessDeniedCustomException("Bạn không được phép tạo chi nhánh mới. Chỉ System Admin mới có quyền này.");
        }

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
        assignManagedBranchAdmin(savedBranch, request.getManagedBranchAdminId());
        replaceAmenities(savedBranch, request.getAmenityNames());
        saveUploadedImages(savedBranch, request.getImageFiles());

        return getBranchById(savedBranch.getId());
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch branch = branchRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chi nhánh"));

        branchSecurityService.ensureUserCanManageBranch(id);

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

        reassignManagedBranchAdmin(branch, request.getManagedBranchAdminId());
        replaceAmenities(branch, request.getAmenityNames());
        updateImages(branch, request);

        return getBranchById(id);
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chi nhánh"));

        branchSecurityService.ensureUserCanManageBranch(id);
        if (!branch.getIsDeleted()) {
            branch.setIsDeleted(true);
            branchRepository.save(branch);
        }
    }

    @Override
    @Transactional
    public void toggleBranchStatus(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chi nhánh"));

        branchSecurityService.ensureUserCanManageBranch(id);
        branch.setStatus(branch.getStatus() == BranchStatus.OPEN ? BranchStatus.CLOSED : BranchStatus.OPEN);
        branchRepository.save(branch);
    }

    private void assignManagedBranchAdmin(Branch branch, Long branchAdminId) {
        if (branchAdminId == null) {
            return;
        }

        User branchAdmin = userRepository.findById(branchAdminId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Branch Admin"));
        branchAdmin.setManagedBranch(branch);
        userRepository.save(branchAdmin);
    }

    private void reassignManagedBranchAdmin(Branch branch, Long branchAdminId) {
        List<User> currentAdmins = userRepository.findAllByIsDeletedFalseAndManagedBranchId(branch.getId());
        for (User admin : currentAdmins) {
            admin.setManagedBranch(null);
            userRepository.save(admin);
        }
        assignManagedBranchAdmin(branch, branchAdminId);
    }

    private void replaceAmenities(Branch branch, String[] amenityNames) {
        if (amenityNames == null) {
            return;
        }

        branchAmenityRepository.deleteAllByBranchId(branch.getId());
        for (String amenityName : amenityNames) {
            if (amenityName != null && !amenityName.trim().isEmpty()) {
                branchAmenityRepository.save(BranchAmenity.builder()
                        .branch(branch)
                        .amenityName(amenityName.trim())
                        .build());
            }
        }
    }

    private void saveUploadedImages(Branch branch, MultipartFile[] imageFiles) {
        if (imageFiles == null || imageFiles.length == 0) {
            return;
        }

        for (MultipartFile file : imageFiles) {
            if (file != null && !file.isEmpty()) {
                String storedPath = imageStorageService.storeImage(file, "branches");
                branchImageRepository.save(BranchImage.builder()
                        .branch(branch)
                        .imageUrl(storedPath)
                        .build());
            }
        }
    }

    private void updateImages(Branch branch, BranchRequest request) {
        MultipartFile[] imageFiles = request.getImageFiles();
        String[] existingImageUrls = request.getExistingImageUrls();
        String[] imageUrls = request.getImageUrls();

        boolean hasNewFiles = imageFiles != null && Arrays.stream(imageFiles).anyMatch(file -> file != null && !file.isEmpty());
        boolean hasExplicitExisting = existingImageUrls != null;
        boolean hasLegacyImages = imageUrls != null && imageUrls.length > 0;

        if (hasNewFiles) {
            replaceAllImages(branch, imageFiles);
            return;
        }

        if (hasExplicitExisting) {
            keepOnlySelectedImages(branch, existingImageUrls);
            return;
        }

        if (hasLegacyImages) {
            replaceImagesFromUrls(branch, imageUrls);
        }
    }

    private void replaceAllImages(Branch branch, MultipartFile[] imageFiles) {
        deleteAllStoredImages(branch.getId());
        branchImageRepository.deleteAllByBranchId(branch.getId());
        saveUploadedImages(branch, imageFiles);
    }

    private void keepOnlySelectedImages(Branch branch, String[] existingImageUrls) {
        List<String> normalizedKeepUrls = Arrays.stream(existingImageUrls)
                .map(this::normalizeStoredImageUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<BranchImage> oldImages = branchImageRepository.findAllByBranchId(branch.getId());
        for (BranchImage oldImage : oldImages) {
            if (!normalizedKeepUrls.contains(oldImage.getImageUrl())) {
                deleteStoredImageIfNeeded(oldImage.getImageUrl());
                branchImageRepository.delete(oldImage);
            }
        }
    }

    private void replaceImagesFromUrls(Branch branch, String[] imageUrls) {
        deleteAllStoredImages(branch.getId());
        branchImageRepository.deleteAllByBranchId(branch.getId());

        for (String imageUrl : imageUrls) {
            String normalizedImageUrl = normalizeStoredImageUrl(imageUrl);
            if (normalizedImageUrl != null) {
                branchImageRepository.save(BranchImage.builder()
                        .branch(branch)
                        .imageUrl(normalizedImageUrl)
                        .build());
            }
        }
    }

    private void deleteAllStoredImages(Long branchId) {
        List<BranchImage> oldImages = branchImageRepository.findAllByBranchId(branchId);
        for (BranchImage oldImage : oldImages) {
            deleteStoredImageIfNeeded(oldImage.getImageUrl());
        }
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
                .map(this::toPublicImageUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                : List.of();

        BigDecimal minPrice = branchRepository.findMinPriceByBranchId(branch.getId());
        Integer courtCount = branchRepository.countCourtsByBranchId(branch.getId());

        List<User> branchAdmins = userRepository.findAllByIsDeletedFalseAndManagedBranchId(branch.getId());
        Long adminId = branchAdmins.isEmpty() ? null : branchAdmins.get(0).getId();
        String adminName = branchAdmins.isEmpty() ? null : branchAdmins.get(0).getFullName();
        String adminEmail = branchAdmins.isEmpty() ? null : branchAdmins.get(0).getEmail();

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
                .managedBranchAdminId(adminId)
                .managedBranchAdminName(adminName)
                .managedBranchAdminEmail(adminEmail)
                .build();
    }

    private String normalizeStoredImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        String trimmedImageUrl = imageUrl.trim();
        if (isRemoteImageUrl(trimmedImageUrl)) {
            return trimmedImageUrl;
        }
        if (trimmedImageUrl.startsWith("/uploads/")) {
            return trimmedImageUrl.substring("/uploads/".length());
        }
        if (trimmedImageUrl.startsWith("uploads/")) {
            return trimmedImageUrl.substring("uploads/".length());
        }
        return trimmedImageUrl.startsWith("/") ? trimmedImageUrl.substring(1) : trimmedImageUrl;
    }

    private String toPublicImageUrl(String imageUrl) {
        String normalizedImageUrl = normalizeStoredImageUrl(imageUrl);
        if (normalizedImageUrl == null) {
            return null;
        }
        if (isRemoteImageUrl(normalizedImageUrl)) {
            return normalizedImageUrl;
        }
        return "/uploads/" + normalizedImageUrl;
    }

    private void deleteStoredImageIfNeeded(String imageUrl) {
        if (!isRemoteImageUrl(imageUrl)) {
            imageStorageService.deleteImage(imageUrl);
        }
    }

    private boolean isRemoteImageUrl(String imageUrl) {
        if (imageUrl == null) {
            return false;
        }
        String normalized = imageUrl.trim().toLowerCase();
        return normalized.startsWith("http://") || normalized.startsWith("https://");
    }
}
