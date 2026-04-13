package com.badminton.booking.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    COURT_NOT_FOUND(1007, "Không tìm thấy sân"),
    TIMESLOT_NOT_FOUND(1008, "Không tìm thấy khung giờ"),
    PRICE_NOT_FOUND(1009, "Không tìm thấy giá"),
    TIMESLOT_ALREADY_BOOKED(409, "Sân đã được đặt"),
    BOOKING_NOT_FOUND(404, "Không tìm thấy booking"),
    INVALID_REQUEST(401, "Dữ liệu không hợp lệ"),
    DUPLICATE_SLOT_IN_REQUEST(402, "Slot bị lặp trong request"),
    CANCELLATION_WINDOW_EXPIRED(410, "Chỉ được hủy sân trước 30 phút so với giờ bắt đầu"),
    NOT_FOUND(404, "Không tìm thấy"),
    CONFLICT(409, "Dữ liệu đã tồn tại"),
    REVIEW_ALREADY_EXISTS(409, "Bạn đã đánh giá sân này cho booking này rồi"),
    BOOKING_CANNOT_BE_REVIEWED(400, "Booking chưa hoàn thành hoặc không thuộc tài khoản của bạn"),
    BRANCH_NOT_FOUND(404, "Không tìm thấy cơ sở");

    private final int code;
    private final String message;
}
