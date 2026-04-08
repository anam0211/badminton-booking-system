package com.badminton.booking.booking.controller;

import com.badminton.booking.booking.dto.response.BookingCreatePageData;
import com.badminton.booking.booking.dto.response.BookingPageMode;
import com.badminton.booking.booking.dto.response.BookingResultPageData;
import com.badminton.booking.booking.service.BookingPageService;
import com.badminton.booking.booking.service.BookingService;
import com.badminton.booking.booking.support.BookingFormSupport;
import com.badminton.booking.booking.support.BookingPageModelBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Objects;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingPageController {

    private final BookingService bookingService;
    private final BookingPageService bookingPageService;
    private final BookingFormSupport bookingFormSupport;
    private final BookingPageModelBinder bookingPageModelBinder;

    @GetMapping("/create")
    public String showCreateForm(@RequestParam(defaultValue = "1") Long userId,
                                 @RequestParam(required = false) Long branchId,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate playDate,
                                 Model model) {

        bookingFormSupport.ensureBookingRequest(model, branchId);

        BookingCreatePageData pageData = bookingPageService.buildCreatePageData(
                userId,
                branchId,
                Objects.requireNonNullElse(playDate, LocalDate.now())
        );
        bookingPageModelBinder.bindCreatePage(model, pageData);

        return "booking/create";
    }

    @GetMapping("/history")
    public String bookingHistory(@RequestParam Long userId, Model model) {
        bookingPageModelBinder.bindListPage(model, bookingPageService.buildHistoryPageData(userId));
        return "booking/list";
    }

    @GetMapping("/{bookingId}")
    public String bookingDetail(@PathVariable Long bookingId, Model model) {
        BookingResultPageData pageData = bookingPageService.buildResultPageData(
                bookingService.getBookingById(bookingId),
                BookingPageMode.DETAIL
        );
        bookingPageModelBinder.bindResultPage(model, pageData);
        return "booking/success";
    }
}
