package com.badminton.booking.booking.controller;

import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.service.BookingService;
import com.badminton.booking.common.base.BaseResponse;
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.user.service.UserService;
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
    private final UserService userService;

    @GetMapping
    public BaseResponse<List<BookingResponse>> getAllBookings() {
        User currentUser = userService.getCurrentUser();
        if (!hasAdminAccess(currentUser)) {
            return BaseResponse.<List<BookingResponse>>builder()
                    .success(false)
                    .message("Ban khong co quyen xem danh sach booking")
                    .build();
        }

        return BaseResponse.<List<BookingResponse>>builder()
                .success(true)
                .message("Lay danh sach booking thanh cong")
                .data(bookingService.getAllBookingsForViewer(currentUser))
                .build();
    }

    @PutMapping("/{bookingId}/cancel")
    public BaseResponse<BookingResponse> cancelBooking(@PathVariable Long bookingId) {
        User currentUser = userService.getCurrentUser();
        if (!hasAdminAccess(currentUser)) {
            return BaseResponse.<BookingResponse>builder()
                    .success(false)
                    .message("Ban khong co quyen huy booking")
                    .build();
        }

        try {
            return BaseResponse.<BookingResponse>builder()
                    .success(true)
                    .message("Huy san thanh cong")
                    .data(bookingService.cancelBookingForViewer(bookingId, currentUser))
                    .build();
        } catch (AppException ex) {
            return BaseResponse.<BookingResponse>builder()
                    .success(false)
                    .message(ex.getMessage())
                    .build();
        }
    }

    private boolean hasAdminAccess(User user) {
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            return false;
        }

        String roleName = user.getRole().getName();
        return RoleName.ADMIN.name().equalsIgnoreCase(roleName)
                || RoleName.BRANCH_ADMIN.name().equalsIgnoreCase(roleName);
    }
}
