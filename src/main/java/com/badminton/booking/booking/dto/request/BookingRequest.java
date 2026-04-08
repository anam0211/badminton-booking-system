package com.badminton.booking.booking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookingRequest {

    @NotNull(message = "Vui l\u00f2ng ch\u1ecdn chi nh\u00e1nh")
    private Long branchId;

    private String note;

    @Valid
    @NotEmpty(message = "Vui l\u00f2ng ch\u1ecdn \u00edt nh\u1ea5t 1 slot")
    private List<Slots> slots = new ArrayList<>();
}
