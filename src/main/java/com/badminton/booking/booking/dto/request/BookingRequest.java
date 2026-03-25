package com.badminton.booking.booking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookingRequest {

    @NotNull(message = "Vui lòng chọn chi nhánh")
    private Long branchId;

    private String note;

    @Valid
    @NotEmpty(message = "Vui lòng chọn ít nhất 1 slot")
    private List<Slots> slots = new ArrayList<>();
}