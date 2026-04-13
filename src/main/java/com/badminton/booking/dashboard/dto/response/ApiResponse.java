package com.badminton.booking.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    int code = 1000;
    @Builder.Default
    String status = "success";
    @Builder.Default
    String message = "Lấy dữ liệu thành công";
    T data;
}
