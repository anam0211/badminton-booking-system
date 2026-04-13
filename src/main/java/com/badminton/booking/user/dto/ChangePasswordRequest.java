package com.badminton.booking.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu hiện tại không được để trống.")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống.")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự.")
    private String newPassword;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu mới.")
    private String confirmNewPassword;
}
