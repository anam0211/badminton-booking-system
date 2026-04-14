package com.badminton.booking.admin.controller;

import com.badminton.booking.admin.service.AdminUserService;
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.enums.UserStatus;
import com.badminton.booking.common.exception.BadRequestException;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public String userManagementPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            Model model
    ) {
        if (!isAdmin(customUserDetails)) {
            return "error/403";
        }

        model.addAttribute("activePage", "users");
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("users", adminUserService.getUsers(keyword));
        model.addAttribute("roleOptions", adminUserService.getRoleOptions());
        model.addAttribute("lockStatus", UserStatus.BANNED.name());
        return "admin/users";
    }

    @PostMapping("/{userId}/role")
    public String updateRole(
            @PathVariable Long userId,
            @RequestParam("roleName") RoleName roleName,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (!isAdmin(customUserDetails)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chỉ ADMIN mới có quyền đổi role.");
                return "redirect:/admin/users";
            }

            User currentUser = customUserDetails.getUser();
            adminUserService.updateUserRole(userId, roleName, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật role thành công.");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/toggle-lock")
    public String toggleLock(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (!isAdmin(customUserDetails)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chỉ ADMIN mới có quyền khóa hoặc mở khóa tài khoản.");
                return "redirect:/admin/users";
            }

            User currentUser = customUserDetails.getUser();
            adminUserService.toggleLockUser(userId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái tài khoản thành công.");
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    private boolean isAdmin(CustomUserDetails customUserDetails) {
        return customUserDetails != null
                && customUserDetails.getUser() != null
                && customUserDetails.getUser().getRole() != null
                && RoleName.ADMIN.name().equalsIgnoreCase(customUserDetails.getUser().getRole().getName());
    }
}
