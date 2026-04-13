package com.badminton.booking.branch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchRequest {

    @NotBlank(message = "Tên chi nhánh không được để trống")
    @Size(max = 255, message = "Tên chi nhánh không được vượt quá 255 ký tự")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Size(max = 500, message = "Link bản đồ không được vượt quá 500 ký tự")
    private String mapLink;

    @NotNull(message = "Khu vực không được để trống")
    private Integer areaId;

    @Builder.Default
    private String status = "OPEN";

    private String description;

    private String[] amenityNames;

    private String[] imageUrls;
}
