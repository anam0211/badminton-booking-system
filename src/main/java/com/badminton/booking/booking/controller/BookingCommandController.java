package com.badminton.booking.booking.controller;

import com.badminton.booking.booking.dto.request.BookingRequest;
import com.badminton.booking.booking.dto.response.BookingCreatePageData;
import com.badminton.booking.booking.dto.response.BookingPageMode;
import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.dto.response.BookingResultPageData;
import com.badminton.booking.booking.service.BookingPageService;
import com.badminton.booking.booking.service.BookingService;
import com.badminton.booking.booking.support.BookingFormSupport;
import com.badminton.booking.booking.support.BookingPageModelBinder;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Slf4j
@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingCommandController {

    private final BookingService bookingService;
    private final BookingPageService bookingPageService;
    private final BookingFormSupport bookingFormSupport;
    private final BookingPageModelBinder bookingPageModelBinder;
    private final UserService userService;

    @PostMapping("/save")
    public String createBooking(@Valid @ModelAttribute("bookingRequest") BookingRequest request,
                                BindingResult bindingResult,
                                Model model) {

        User currentUser = userService.getCurrentUser();
        bookingFormSupport.normalize(request);
        LocalDate playDate = bookingFormSupport.resolvePlayDate(request);

        if (bindingResult.hasErrors()) {
            return renderCreatePage(model, request.getBranchId(), playDate);
        }

        try {
            BookingResponse booking = bookingService.createBooking(request, currentUser.getId());
            BookingResultPageData pageData = bookingPageService.buildResultPageData(booking, BookingPageMode.SUCCESS);
            bookingPageModelBinder.bindResultPage(model, pageData);
            return "booking/success";
        } catch (Exception ex) {
            log.error("Create booking failed: userId={}, branchId={}, playDate={}",
                    currentUser.getId(), request.getBranchId(), playDate, ex);
            model.addAttribute("errorMessage", ex.getMessage());
            return renderCreatePage(model, request.getBranchId(), playDate);
        }
    }

    private String renderCreatePage(Model model, Long branchId, LocalDate playDate) {
        BookingCreatePageData pageData = bookingPageService.buildCreatePageData(branchId, playDate);
        bookingPageModelBinder.bindCreatePage(model, pageData);
        return "booking/create";
    }
}
