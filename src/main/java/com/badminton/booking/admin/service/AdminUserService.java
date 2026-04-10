package com.badminton.booking.admin.service;

import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.enums.UserStatus;
import com.badminton.booking.common.exception.BadRequestException;
import com.badminton.booking.domain.entity.Role;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.domain.repository.RoleRepository;
import com.badminton.booking.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<User> getUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAllActiveUsersWithRole();
        }
        return userRepository.searchUsersWithRole(keyword.trim());
    }

    @Transactional(readOnly = true)
    public List<RoleName> getRoleOptions() {
        return Arrays.asList(RoleName.values());
    }

    @Transactional
    public void updateUserRole(Long userId, RoleName newRoleName, Long currentUserId) {
        User targetUser = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản cần cập nhật."));

        if (targetUser.getId().equals(currentUserId)) {
            throw new BadRequestException("Bạn không thể tự đổi role của chính mình.");
        }

        Role role = roleRepository.findByName(newRoleName.name())
                .orElseThrow(() -> new BadRequestException("Role không hợp lệ."));

        targetUser.setRole(role);
    }

    @Transactional
    public void toggleLockUser(Long userId, Long currentUserId) {
        User targetUser = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản cần khóa/mở khóa."));

        if (targetUser.getId().equals(currentUserId)) {
            throw new BadRequestException("Bạn không thể tự khóa tài khoản của chính mình.");
        }

        if (targetUser.getStatus() == UserStatus.BANNED) {
            targetUser.setStatus(UserStatus.ACTIVE);
            return;
        }

        targetUser.setStatus(UserStatus.BANNED);
    }
}
