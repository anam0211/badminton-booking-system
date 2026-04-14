package com.badminton.booking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email khong duoc de trong.")
    @Email(message = "Email khong dung dinh dang.")
    private String email;

    @NotBlank(message = "Mat khau khong duoc de trong.")
    private String password;
}
