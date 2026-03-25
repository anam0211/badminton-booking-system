package com.badminton.booking.booking.controller;

import com.badminton.booking.booking.dto.request.BookingRequest;
import com.badminton.booking.booking.dto.request.Slots;
import com.badminton.booking.booking.dto.response.BookingCreatePageData;
import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.booking.service.BookingPageService;
import com.badminton.booking.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;

@Slf4j
@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingPageService bookingPageService;

    @GetMapping("/create")
    public String showCreateForm(@RequestParam(defaultValue = "1") Long userId,
                                 @RequestParam(required = false) Long branchId,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate playDate,
                                 Model model) {

        LocalDate resolvedPlayDate = playDate != null ? playDate : LocalDate.now();
        initializeBookingRequestIfAbsent(model, branchId);

        BookingCreatePageData pageData =
                bookingPageService.buildCreatePageData(userId, branchId, resolvedPlayDate);

        bindCreatePageData(model, pageData);
        return "booking/create";
    }

    @PostMapping("/save")
    public String createBooking(@RequestParam Long userId,
                                @Valid @ModelAttribute("bookingRequest") BookingRequest request,
                                BindingResult bindingResult,
                                Model model) {

        normalizeRequest(request);
        LocalDate playDate = extractPlayDate(request);

        if (bindingResult.hasErrors()) {
            BookingCreatePageData pageData =
                    bookingPageService.buildCreatePageData(userId, request.getBranchId(), playDate);
            bindCreatePageData(model, pageData);
            return "booking/create";
        }

        try {
            BookingResponse booking = bookingService.createBooking(request, userId);
            model.addAttribute("booking", booking);
            return "booking/success";
        } catch (Exception ex) {
            log.error("Create booking failed: userId={}, branchId={}, playDate={}",
                    userId, request.getBranchId(), playDate, ex);

            model.addAttribute("errorMessage", ex.getMessage());

            BookingCreatePageData pageData =
                    bookingPageService.buildCreatePageData(userId, request.getBranchId(), playDate);
            bindCreatePageData(model, pageData);

            return "booking/create";
        }
    }

    @GetMapping("/{bookingId}")
    public String bookingDetail(@PathVariable Long bookingId, Model model) {
        BookingResponse booking = bookingService.getBookingById(bookingId);
        model.addAttribute("booking", booking);
        return "booking/success";
    }

    private void initializeBookingRequestIfAbsent(Model model, Long branchId) {
        if (model.containsAttribute("bookingRequest")) {
            return;
        }

        BookingRequest request = new BookingRequest();
        request.setBranchId(branchId);
        request.setSlots(new ArrayList<>());
        model.addAttribute("bookingRequest", request);
    }

    private void normalizeRequest(BookingRequest request) {
        if (request.getSlots() == null) {
            request.setSlots(new ArrayList<>());
        }
    }

    private void bindCreatePageData(Model model, BookingCreatePageData pageData) {
        model.addAttribute("userId", pageData.getUserId());
        model.addAttribute("playDate", pageData.getPlayDate());
        model.addAttribute("branches", pageData.getBranches());
        model.addAttribute("courts", pageData.getCourts());
        model.addAttribute("timeSlots", pageData.getTimeSlots());
        model.addAttribute("slotGrid", pageData.getSlotGrid());
    }

    private LocalDate extractPlayDate(BookingRequest request) {
        if (request.getSlots() != null && !request.getSlots().isEmpty()) {
            for (Slots slot : request.getSlots()) {
                if (slot.getPlayDate() != null) {
                    return slot.getPlayDate();
                }
            }
        }
        return LocalDate.now();
    }
}