package com.badminton.booking.booking.support;

import com.badminton.booking.booking.dto.response.BookingCreatePageData;
import com.badminton.booking.booking.dto.response.BookingResultPageData;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class BookingPageModelBinder {

    public void bindCreatePage(Model model, BookingCreatePageData pageData) {
        model.addAttribute("userId", pageData.getUserId());
        model.addAttribute("playDate", pageData.getPlayDate());
        model.addAttribute("branches", pageData.getBranches());
        model.addAttribute("courts", pageData.getCourts());
        model.addAttribute("timeSlots", pageData.getTimeSlots());
        model.addAttribute("slotGrid", pageData.getSlotGrid());
    }

    public void bindResultPage(Model model, BookingResultPageData pageData) {
        model.addAttribute("booking", pageData.getBooking());
        model.addAttribute("pageMode", pageData.getPageMode().getValue());
    }
}
