package com.badminton.booking.config;

import com.badminton.booking.domain.repository.UserRepository;
import com.badminton.booking.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserModelAdvice {

    private final UserRepository userRepository;

    @ModelAttribute("headerUserName")
    public String headerUserName() {
        return SecurityUtils.getCurrentUsername()
                .flatMap(userRepository::findByEmailIgnoreCase)
                .map(user -> user.getFullName())
                .orElse("Guest");
    }

    @ModelAttribute("headerUserEmail")
    public String headerUserEmail() {
        return SecurityUtils.getCurrentUsername().orElse("");
    }
}
