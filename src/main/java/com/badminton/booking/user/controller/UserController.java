package com.badminton.booking.user.controller;

import com.badminton.booking.domain.entity.User;
import com.badminton.booking.user.dto.ChangePasswordRequest;
import com.badminton.booking.user.dto.ProfileUpdateRequest;
import com.badminton.booking.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public String profilePage(Model model) {
        User currentUser = userService.getCurrentUser();
        ProfileUpdateRequest profileForm = new ProfileUpdateRequest();
        profileForm.setFullName(currentUser.getFullName());
        profileForm.setPhone(currentUser.getPhone());
        profileForm.setAvatarUrl(currentUser.getAvatarUrl());

        model.addAttribute("currentUserDetail", currentUser);
        model.addAttribute("profileForm", profileForm);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileUpdateRequest profileForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUserDetail", userService.getCurrentUser());
            return "user/profile";
        }

        userService.updateProfile(profileForm);
        redirectAttributes.addFlashAttribute("successMessage", "Cap nhat profile thanh cong.");
        return "redirect:/user/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(@ModelAttribute("passwordForm") ChangePasswordRequest changePasswordRequest) {
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("passwordForm") ChangePasswordRequest changePasswordRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "user/change-password";
        }

        userService.changePassword(changePasswordRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Doi mat khau thanh cong.");
        return "redirect:/user/change-password";
    }

    @GetMapping("/me")
    @ResponseBody
    public Map<String, Object> currentUserInfo() {
        User user = userService.getCurrentUser();
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "role", user.getRole().getName()
        );
    }
}
