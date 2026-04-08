package com.badminton.booking.config;

import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.enums.UserStatus;
import com.badminton.booking.domain.entity.Role;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.domain.repository.RoleRepository;
import com.badminton.booking.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.ADMIN.name())
                        .build()));

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.CUSTOMER.name())
                        .build()));

        roleRepository.findByName(RoleName.BRANCH_ADMIN.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.BRANCH_ADMIN.name())
                        .build()));

        if (!userRepository.existsByEmailIgnoreCase("admin@demo.local")) {
            userRepository.save(User.builder()
                    .email("admin@demo.local")
                    .password(passwordEncoder.encode("123456"))
                    .fullName("System Admin")
                    .role(adminRole)
                    .status(UserStatus.ACTIVE)
                    .isDeleted(false)
                    .build());
        }

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
