package com.badminton.booking.admin.controller;

import com.badminton.booking.area.dto.response.AreaResponse;
import com.badminton.booking.area.service.AreaService;
import com.badminton.booking.branch.dto.request.BranchRequest;
import com.badminton.booking.branch.dto.response.BranchListItem;
import com.badminton.booking.branch.dto.response.BranchResponse;
import com.badminton.booking.branch.service.BranchService;
import com.badminton.booking.common.enums.BranchStatus;
import com.badminton.booking.common.exception.AppException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/branches")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminBranchController {

    BranchService branchService;
    AreaService areaService;

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
        List<AreaResponse> areas = areaService.getAllAreas();
        model.addAttribute("activePage", "branches");
        model.addAttribute("branch", new BranchRequest());
        model.addAttribute("areas", areas);
        model.addAttribute("statusOptions", BranchStatus.values());
        model.addAttribute("isEdit", false);
        return "admin/branch/create";
    }

    @PostMapping("/create")
    public String createBranch(
            @Valid @ModelAttribute("branch") BranchRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            List<AreaResponse> areas = areaService.getAllAreas();
            model.addAttribute("activePage", "branches");
            model.addAttribute("areas", areas);
            model.addAttribute("statusOptions", BranchStatus.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin");
            return "admin/branch/create";
        }

        try {
            BranchResponse branch = branchService.createBranch(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm chi nhánh thành công!");
            return "redirect:/admin/branches/detail/" + branch.getId();
        } catch (AppException ex) {
            List<AreaResponse> areas = areaService.getAllAreas();
            model.addAttribute("activePage", "branches");
            model.addAttribute("areas", areas);
            model.addAttribute("statusOptions", BranchStatus.values());
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
            request.setImageUrls(branch.getImages() != null ? branch.getImages().toArray(new String[0]) : null);

            List<AreaResponse> areas = areaService.getAllAreas();
            model.addAttribute("activePage", "branches");
            model.addAttribute("branch", request);
            model.addAttribute("branchId", id);
            model.addAttribute("areas", areas);
            model.addAttribute("statusOptions", BranchStatus.values());
            model.addAttribute("isEdit", true);
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
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            List<AreaResponse> areas = areaService.getAllAreas();
            model.addAttribute("activePage", "branches");
            model.addAttribute("areas", areas);
            model.addAttribute("statusOptions", BranchStatus.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("areaId", id);
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin");
            return "admin/branch/edit";
        }

        try {
            BranchResponse branch = branchService.updateBranch(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chi nhánh thành công!");
            return "redirect:/admin/branches/detail/" + branch.getId();
        } catch (AppException ex) {
            List<AreaResponse> areas = areaService.getAllAreas();
            model.addAttribute("activePage", "branches");
            model.addAttribute("areas", areas);
            model.addAttribute("statusOptions", BranchStatus.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("areaId", id);
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/branch/edit";
        }
    }

    @GetMapping("/detail/{id}")
    public String branchDetail(@PathVariable Long id, Model model) {
        try {
            BranchResponse branch = branchService.getBranchDetail(id);
            model.addAttribute("activePage", "branches");
            model.addAttribute("branch", branch);
            return "admin/branch/detail";
        } catch (AppException ex) {
            return "redirect:/admin/branches";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteBranch(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            branchService.deleteBranch(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa chi nhánh thành công!");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/branches";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            branchService.toggleBranchStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/branches/detail/" + id;
    }
}
