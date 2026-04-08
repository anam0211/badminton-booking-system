package com.badminton.booking.booking.controller;

import com.badminton.booking.booking.service.BookingPageService;
import com.badminton.booking.booking.support.BookingPageModelBinder;
import com.badminton.booking.common.enums.RoleName;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingPageController {

    private final BookingPageService bookingPageService;
    private final BookingPageModelBinder bookingPageModelBinder;
    private final UserService userService;

    @GetMapping
    public String bookingList(Model model) {
        User currentUser = userService.getCurrentUser();
        if (!hasAdminAccess(currentUser)) {
            model.addAttribute("errorMessage", "B\u1ea1n kh\u00f4ng c\u00f3 quy\u1ec1n xem danh s\u00e1ch booking c\u1ee7a admin.");
            return "error/403";
        }

        bookingPageModelBinder.bindListPage(model, bookingPageService.buildAdminBookingListPageData(currentUser));
        return "booking/list";
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
