package com.badminton.booking.admin.controller;

import com.badminton.booking.area.dto.request.AreaRequest;
import com.badminton.booking.area.dto.response.AreaResponse;
import com.badminton.booking.area.service.AreaService;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/areas")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAreaController {

    AreaService areaService;

    @GetMapping
    public String listAreas(Model model) {
        List<AreaResponse> areas = areaService.getAllAreas();
        model.addAttribute("activePage", "areas");
        model.addAttribute("areas", areas);
        return "admin/area/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("activePage", "areas");
        model.addAttribute("area", new AreaRequest());
        model.addAttribute("isEdit", false);
        return "admin/area/create";
    }

    @PostMapping("/create")
    public String createArea(
            @Valid @ModelAttribute("area") AreaRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "areas");
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin");
            return "admin/area/create";
        }

        try {
            areaService.createArea(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm khu vực thành công!");
            return "redirect:/admin/areas";
        } catch (AppException ex) {
            model.addAttribute("activePage", "areas");
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/area/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        try {
            AreaResponse area = areaService.getAreaById(id);
            AreaRequest request = new AreaRequest();
            request.setName(area.getName());
            model.addAttribute("activePage", "areas");
            model.addAttribute("area", request);
            model.addAttribute("areaId", id);
            model.addAttribute("isEdit", true);
            return "admin/area/edit";
        } catch (AppException ex) {
            return "redirect:/admin/areas";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateArea(
            @PathVariable Integer id,
            @Valid @ModelAttribute("area") AreaRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "areas");
            model.addAttribute("isEdit", true);
            model.addAttribute("areaId", id);
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin");
            return "admin/area/edit";
        }

        try {
            areaService.updateArea(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khu vực thành công!");
            return "redirect:/admin/areas";
        } catch (AppException ex) {
            model.addAttribute("activePage", "areas");
            model.addAttribute("isEdit", true);
            model.addAttribute("areaId", id);
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/area/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteArea(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            areaService.deleteArea(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa khu vực thành công!");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/areas";
    }
}
