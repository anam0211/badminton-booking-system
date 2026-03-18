package com.badminton.booking.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Mat khau hien tai khong duoc de trong.")
    private String currentPassword;

    @NotBlank(message = "Mat khau moi khong duoc de trong.")
    @Size(min = 6, message = "Mat khau toi thieu 6 ky tu.")
    private String newPassword;
}
