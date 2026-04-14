package com.badminton.booking.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    COURT_NOT_FOUND(1007, "Kh\u00f4ng t\u00ecm th\u1ea5y s\u00e2n"),
    TIMESLOT_NOT_FOUND(1008, "Kh\u00f4ng t\u00ecm th\u1ea5y khung gi\u1edd"),
    PRICE_NOT_FOUND(1009, "Kh\u00f4ng t\u00ecm th\u1ea5y gi\u00e1"),
    TIMESLOT_ALREADY_BOOKED(409, "S\u00e2n \u0111\u00e3 \u0111\u01b0\u1ee3c \u0111\u1eb7t"),
    BOOKING_NOT_FOUND(404, "Kh\u00f4ng t\u00ecm th\u1ea5y booking"),
    INVALID_REQUEST(401, "D\u1eef li\u1ec7u kh\u00f4ng h\u1ee3p l\u1ec7"),
    DUPLICATE_SLOT_IN_REQUEST(402, "Slot b\u1ecb l\u1eb7p trong request"),
    CANCELLATION_WINDOW_EXPIRED(410, "Ch\u1ec9 \u0111\u01b0\u1ee3c h\u1ee7y s\u00e2n tr\u01b0\u1edbc 30 ph\u00fat so v\u1edbi gi\u1edd b\u1eaft \u0111\u1ea7u");

    private final int code;
    private final String message;
}
