package com.badminton.booking.auth.service;

import com.badminton.booking.auth.dto.LoginRequest;
import com.badminton.booking.auth.dto.RegisterRequest;
import com.badminton.booking.auth.dto.TokenResponse;
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.enums.UserStatus;
import com.badminton.booking.common.exception.BadRequestException;
import com.badminton.booking.domain.entity.Role;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.domain.repository.RoleRepository;
import com.badminton.booking.domain.repository.UserRepository;
import com.badminton.booking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email da ton tai.");
        }//check mail

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER.name())// roleid
                .orElseThrow(() -> new BadRequestException("Chua khoi tao role CUSTOMER."));

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone())
                .role(customerRole)
                .status(UserStatus.ACTIVE)
                .isDeleted(false)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate( 
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        ); // xác thực tk+mk đúng thì trả về authentication sai thì throw exception

        UserDetails principal = (UserDetails) authentication.getPrincipal(); // principal gồm : username, password, authorities( role)
        String accessToken = jwtService.generateToken(principal);

        return TokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .build();
    }

    public void logout() {
        // Khong luu state tren server khi chi dung access token.
    }
}
