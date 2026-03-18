package com.badminton.booking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email khong duoc de trong.")
    @Email(message = "Email khong dung dinh dang.")
    private String email;

    @NotBlank(message = "Ho ten khong duoc de trong.")
    private String fullName;

    @NotBlank(message = "Mat khau khong duoc de trong.")
    @Size(min = 6, message = "Mat khau toi thieu 6 ky tu.")
    private String password;

    private String phone;
}
