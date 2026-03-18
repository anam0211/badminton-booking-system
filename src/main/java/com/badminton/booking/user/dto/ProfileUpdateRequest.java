package com.badminton.booking.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "Ho ten khong duoc de trong.")
    private String fullName;

    private String phone;

    private String avatarUrl;
}
