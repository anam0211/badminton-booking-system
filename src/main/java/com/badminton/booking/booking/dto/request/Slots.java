package com.badminton.booking.booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Slots {

    @NotNull(message = "Vui lòng chọn sân")
    private Long courtId;

    @NotNull(message = "Vui lòng chọn khung giờ")
    private Integer timeSlotId;

    @NotNull(message = "Vui lòng chọn ngày chơi")
    @FutureOrPresent(message = "Ngày chơi phải từ hôm nay trở đi")
    private LocalDate playDate;
}