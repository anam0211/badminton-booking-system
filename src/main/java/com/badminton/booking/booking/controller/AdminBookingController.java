package com.badminton.booking.booking.controller;

import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.service.BookingService;
import com.badminton.booking.common.base.BaseResponse;
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.common.exception.AccessDeniedCustomException;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<List<BookingResponse>>> getAllBookings() {
        try {
            User currentUser = userService.getCurrentUser();
            if (!hasAdminAccess(currentUser)) {
                return buildListResponse(HttpStatus.FORBIDDEN, "Bạn không có quyền xem danh sách booking.", null);
            }

            return buildListResponse(
                    HttpStatus.OK,
                    "Lấy danh sách booking thành công.",
                    bookingService.getAllBookingsForViewer(currentUser)
            );
        } catch (AccessDeniedCustomException ex) {
            return buildListResponse(resolveAccessDeniedStatus(ex), ex.getMessage(), null);
        } catch (Exception ex) {
            return buildListResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể tải danh sách booking. Vui lòng thử lại.", null);
        }
    }

    @PutMapping(value = "/{bookingId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<BookingResponse>> cancelBooking(@PathVariable Long bookingId) {
        try {
            User currentUser = userService.getCurrentUser();
            if (!hasAdminAccess(currentUser)) {
                return buildBookingResponse(HttpStatus.FORBIDDEN, "Bạn không có quyền hủy booking.", null);
            }

            BookingResponse booking = bookingService.cancelBookingForViewer(bookingId, currentUser);
            return buildBookingResponse(HttpStatus.OK, "Hủy sân thành công.", booking);
        } catch (AccessDeniedCustomException ex) {
            return buildBookingResponse(resolveAccessDeniedStatus(ex), ex.getMessage(), null);
        } catch (AppException ex) {
            return buildBookingResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        } catch (Exception ex) {
            return buildBookingResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể hủy sân lúc này. Vui lòng thử lại.", null);
        }
    }

    private ResponseEntity<BaseResponse<List<BookingResponse>>> buildListResponse(HttpStatus status,
                                                                                  String message,
                                                                                  List<BookingResponse> data) {
        return ResponseEntity.status(status)
                .body(BaseResponse.<List<BookingResponse>>builder()
                        .success(status.is2xxSuccessful())
                        .message(message)
                        .data(data)
                        .build());
    }

    private ResponseEntity<BaseResponse<BookingResponse>> buildBookingResponse(HttpStatus status,
                                                                               String message,
                                                                               BookingResponse data) {
        return ResponseEntity.status(status)
                .body(BaseResponse.<BookingResponse>builder()
                        .success(status.is2xxSuccessful())
                        .message(message)
                        .data(data)
                        .build());
    }

    private HttpStatus resolveAccessDeniedStatus(AccessDeniedCustomException ex) {
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("đăng nhập")) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.FORBIDDEN;
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
