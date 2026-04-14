package com.badminton.booking.admin.controller;

import com.badminton.booking.admin.dto.BranchAdminInfo;
import com.badminton.booking.area.dto.response.AreaResponse;
import com.badminton.booking.area.service.AreaService;
import com.badminton.booking.branch.dto.request.BranchRequest;
import com.badminton.booking.branch.dto.response.BranchListItem;
import com.badminton.booking.branch.dto.response.BranchResponse;
import com.badminton.booking.branch.service.BranchService;
import com.badminton.booking.common.enums.BranchStatus;
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.domain.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/branches")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminBranchController {

    BranchService branchService;
    AreaService areaService;
    UserRepository userRepository;

    @GetMapping
    public String listBranches(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer areaId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<BranchListItem> branchPage = branchService.searchBranches(keyword, areaId, status, page, size);
        List<AreaResponse> areas = areaService.getAllAreas();

        model.addAttribute("activePage", "branches");
        model.addAttribute("branchPage", branchPage);
        model.addAttribute("branches", branchPage.getContent());
        model.addAttribute("areas", areas);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedArea", areaId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statusOptions", BranchStatus.values());

        return "admin/branch/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        populateBranchFormOptions(model);
        model.addAttribute("activePage", "branches");
        model.addAttribute("branch", new BranchRequest());
        model.addAttribute("isEdit", false);
        return "admin/branch/create";
    }

    @PostMapping("/create")
    public String createBranch(
            @Valid @ModelAttribute("branch") BranchRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            populateBranchFormOptions(model);
            model.addAttribute("activePage", "branches");
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin.");
            return "admin/branch/create";
        }

        try {
            request.setImageFiles(imageFiles);
            BranchResponse branch = branchService.createBranch(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm chi nhánh thành công.");
            return "redirect:/admin/branches/detail/" + branch.getId();
        } catch (AppException ex) {
            populateBranchFormOptions(model);
            model.addAttribute("activePage", "branches");
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/branch/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            BranchResponse branch = branchService.getBranchById(id);
            BranchRequest request = new BranchRequest();
            request.setName(branch.getName());
            request.setAddress(branch.getAddress());
            request.setMapLink(branch.getMapLink());
            request.setAreaId(branch.getAreaId());
            request.setStatus(branch.getStatus() != null ? branch.getStatus().name() : "OPEN");
            request.setAmenityNames(branch.getAmenities() != null ? branch.getAmenities().toArray(new String[0]) : null);
            request.setExistingImageUrls(branch.getImages() != null ? branch.getImages().toArray(new String[0]) : null);
            request.setManagedBranchAdminId(branch.getManagedBranchAdminId());

            populateBranchFormOptions(model);
            model.addAttribute("activePage", "branches");
            model.addAttribute("branch", request);
            model.addAttribute("branchId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("currentBranchAdmin", buildCurrentBranchAdmin(branch));
            return "admin/branch/edit";
        } catch (AppException ex) {
            return "redirect:/admin/branches";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateBranch(
            @PathVariable Long id,
            @Valid @ModelAttribute("branch") BranchRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
            @RequestParam(value = "existingImageUrls", required = false) String[] existingImageUrls,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            populateBranchFormOptions(model);
            model.addAttribute("activePage", "branches");
            model.addAttribute("isEdit", true);
            model.addAttribute("branchId", id);
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin.");
            return "admin/branch/edit";
        }

        try {
            request.setImageFiles(imageFiles);
            request.setExistingImageUrls(existingImageUrls);
            BranchResponse branch = branchService.updateBranch(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chi nhánh thành công.");
            return "redirect:/admin/branches/detail/" + branch.getId();
        } catch (AppException ex) {
            populateBranchFormOptions(model);
            model.addAttribute("activePage", "branches");
            model.addAttribute("isEdit", true);
            model.addAttribute("branchId", id);
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/branch/edit";
        }
    }

    @GetMapping("/detail/{id}")
    public String branchDetail(@PathVariable Long id, Model model) {
        try {
            BranchResponse branch = branchService.getBranchById(id);
            model.addAttribute("activePage", "branches");
            model.addAttribute("branch", branch);
            return "admin/branch/detail";
        } catch (AppException ex) {
            return "redirect:/admin/branches";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteBranch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            branchService.deleteBranch(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa chi nhánh thành công.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/branches";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            branchService.toggleBranchStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/branches/detail/" + id;
    }

    private void populateBranchFormOptions(Model model) {
        List<AreaResponse> areas = areaService.getAllAreas();
        List<BranchAdminInfo> branchAdminInfos = userRepository
                .findByRoleNameAndIsDeletedFalseAndManagedBranchIsNull(RoleName.BRANCH_ADMIN.name())
                .stream()
                .map(this::toBranchAdminInfo)
                .toList();

        model.addAttribute("areas", areas);
        model.addAttribute("statusOptions", BranchStatus.values());
        model.addAttribute("branchAdminList", branchAdminInfos);
    }

    private BranchAdminInfo buildCurrentBranchAdmin(BranchResponse branch) {
        if (branch.getManagedBranchAdminId() == null) {
            return null;
        }
        return userRepository.findById(branch.getManagedBranchAdminId())
                .map(this::toBranchAdminInfo)
                .orElse(null);
    }

    private BranchAdminInfo toBranchAdminInfo(User user) {
        return BranchAdminInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }
}
