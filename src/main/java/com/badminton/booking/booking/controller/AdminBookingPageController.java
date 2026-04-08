package com.badminton.booking.booking.controller;

import com.badminton.booking.booking.service.BookingPageService;
import com.badminton.booking.booking.support.BookingPageModelBinder;
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

    @GetMapping
    public String bookingList(Model model) {
        bookingPageModelBinder.bindListPage(model, bookingPageService.buildAdminBookingListPageData());
        return "booking/list";
    }
}
