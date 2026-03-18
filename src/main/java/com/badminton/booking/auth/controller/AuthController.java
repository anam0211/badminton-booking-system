package com.badminton.booking.auth.controller;

import com.badminton.booking.auth.dto.LoginRequest;
import com.badminton.booking.auth.dto.RegisterRequest;
import com.badminton.booking.auth.dto.TokenResponse;
import com.badminton.booking.auth.service.AuthService;
import com.badminton.booking.security.JwtAuthenticationFilter;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; //lombok required args constructor hoặc dùng autowired


// login
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginWithJwt(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpServletResponse response
    ) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        TokenResponse tokenResponse = authService.login(loginRequest);
        response.addCookie(buildAccessTokenCookie(tokenResponse.getAccessToken(), 60 * 60 * 24));
        return "redirect:/user/profile";
    }

// logout
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        authService.logout(); // rỗng vì chỉ dùng access token không refreshtoken
        response.addCookie(buildAccessTokenCookie("", 0));// xoá cookie access token
        return "redirect:/auth/login?logout=true";
    }

// register
    @GetMapping("/register")
    public String registerPage(@ModelAttribute("registerForm") RegisterRequest registerRequest) {
        return "auth/register";
    }
    /* tuong tu (dung cach tren)
    public String registerPage(Model model) {
        RegisterRequest registerRequest = new RegisterRequest();
        model.addAttribute("registerForm", registerRequest);
        return "auth/register";
    }
     */

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerForm") RegisterRequest registerRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        authService.register(registerRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Dang ky thanh cong. Vui long dang nhap.");
        return "redirect:/auth/login";
    }

// build access token cookie
    private Cookie buildAccessTokenCookie(String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(JwtAuthenticationFilter.TOKEN_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }
}
