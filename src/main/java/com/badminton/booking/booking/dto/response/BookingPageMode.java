package com.badminton.booking.booking.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingPageMode {
    SUCCESS("success"),
    DETAIL("detail");

    private final String value;
}
