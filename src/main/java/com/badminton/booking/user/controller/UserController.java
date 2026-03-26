package com.badminton.booking.user.controller;

import com.badminton.booking.domain.entity.User;
import com.badminton.booking.user.dto.ChangePasswordRequest;
import com.badminton.booking.user.dto.ProfileUpdateRequest;
import com.badminton.booking.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @Value("${app.avatar-upload-dir:src/main/resources/static/image}")
    private String avatarUploadDir;

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
            @RequestParam(name = "avatarFile", required = false) MultipartFile avatarFile,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUserDetail", userService.getCurrentUser());
            return "user/profile";
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String savedFileName = saveAvatarFile(avatarFile);
                profileForm.setAvatarUrl(savedFileName);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                bindingResult.rejectValue("avatarUrl", "avatar.invalid", ex.getMessage());
                model.addAttribute("currentUserDetail", userService.getCurrentUser());
                return "user/profile";
            }
        } else if (profileForm.getAvatarUrl() != null) {
            profileForm.setAvatarUrl(profileForm.getAvatarUrl().trim());
        }

        userService.updateProfile(profileForm);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công.");
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

        try {
            userService.changePassword(changePasswordRequest);
        } catch (com.badminton.booking.common.exception.BadRequestException ex) {
            bindingResult.rejectValue("currentPassword", "password.wrong", ex.getMessage());
            return "user/change-password";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công.");
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

    private String saveAvatarFile(MultipartFile avatarFile) {
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("File upload phải là ảnh.");
        }

        String originalName = avatarFile.getOriginalFilename();
        String extension = getFileExtension(originalName);
        String fileName = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Files.copy(avatarFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể lưu ảnh đại diện.", ex);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return ".jpg";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return ".jpg";
        }
        String extension = filename.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (extension.length() > 10) {
            return ".jpg";
        }
        return extension;
    }
}
