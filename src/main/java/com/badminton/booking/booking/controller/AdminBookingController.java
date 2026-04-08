package com.badminton.booking.booking.controller;

import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.service.BookingService;
import com.badminton.booking.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public BaseResponse<List<BookingResponse>> getAllBookings() {
        return BaseResponse.<List<BookingResponse>>builder()
                .success(true)
                .message("Lay danh sach booking thanh cong")
                .data(bookingService.getAllBookings())
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
