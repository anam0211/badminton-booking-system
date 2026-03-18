package com.badminton.booking.config;

import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.enums.UserStatus;
import com.badminton.booking.domain.entity.Permission;
import com.badminton.booking.domain.entity.Role;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.domain.repository.PermissionRepository;
import com.badminton.booking.domain.repository.RoleRepository;
import com.badminton.booking.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Permission viewProfilePermission = permissionRepository.findByCode("USER_VIEW_PROFILE")
                .orElseGet(() -> permissionRepository.save(Permission.builder()
                        .code("USER_VIEW_PROFILE")
                        .name("View profile")
                        .build()));
        Permission updateProfilePermission = permissionRepository.findByCode("USER_UPDATE_PROFILE")
                .orElseGet(() -> permissionRepository.save(Permission.builder()
                        .code("USER_UPDATE_PROFILE")
                        .name("Update profile")
                        .build()));
        Permission changePasswordPermission = permissionRepository.findByCode("USER_CHANGE_PASSWORD")
                .orElseGet(() -> permissionRepository.save(Permission.builder()
                        .code("USER_CHANGE_PASSWORD")
                        .name("Change password")
                        .build()));

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.CUSTOMER.name())
                        .permissions(new HashSet<>(Set.of(
                                viewProfilePermission,
                                updateProfilePermission,
                                changePasswordPermission
                        )))
                        .build()));

        roleRepository.findByName(RoleName.BRANCH_ADMIN.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.BRANCH_ADMIN.name())
                        .permissions(new HashSet<>(Set.of(
                                viewProfilePermission,
                                updateProfilePermission,
                                changePasswordPermission
                        )))
                        .build()));

        if (!userRepository.existsByEmailIgnoreCase("customer@demo.local")) {
            userRepository.save(User.builder()
                    .email("customer@demo.local")
                    .password(passwordEncoder.encode("123456"))
                    .fullName("Demo Customer")
                    .role(customerRole)
                    .status(UserStatus.ACTIVE)
                    .isDeleted(false)
                    .build());
        }
    }
}