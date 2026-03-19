package com.badminton.booking.user.service;

import com.badminton.booking.common.exception.AccessDeniedCustomException;
import com.badminton.booking.common.exception.BadRequestException;
import com.badminton.booking.common.exception.ResourceNotFoundException;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.domain.repository.UserRepository;
import com.badminton.booking.security.SecurityUtils;
import com.badminton.booking.user.dto.ChangePasswordRequest;
import com.badminton.booking.user.dto.ProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        String email = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new AccessDeniedCustomException("Bạn chưa đăng nhập."));
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại."));
    }

    @Transactional
    public User updateProfile(ProfileUpdateRequest request) {
        User user = getCurrentUser();
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setAvatarUrl(request.getAvatarUrl());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
