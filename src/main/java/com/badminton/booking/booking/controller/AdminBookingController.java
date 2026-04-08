package com.badminton.booking.booking.controller;

import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.service.BookingService;
import com.badminton.booking.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @PutMapping("/{bookingId}/approve-payment")
    public BaseResponse<BookingResponse> approvePayment(@PathVariable Long bookingId) {
        return BaseResponse.<BookingResponse>builder()
                .success(true)
                .message("Duyet thanh toan thanh cong")
                .data(bookingService.approvePayment(bookingId))
                .build();
    }

    @PutMapping("/{bookingId}/cancel")
    public BaseResponse<BookingResponse> cancelBooking(@PathVariable Long bookingId) {
        return BaseResponse.<BookingResponse>builder()
                .success(true)
                .message("Huy san thanh cong")
                .data(bookingService.cancelBooking(bookingId))
                .build();
    }
}
