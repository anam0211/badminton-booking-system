package com.badminton.booking.booking.support;

import com.badminton.booking.booking.dto.response.BookingCreatePageData;
import com.badminton.booking.booking.dto.response.BookingListPageData;
import com.badminton.booking.booking.dto.response.BookingResultPageData;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class BookingPageModelBinder {

    public void bindCreatePage(Model model, BookingCreatePageData pageData) {
        model.addAttribute("playDate", pageData.getPlayDate());
        model.addAttribute("selectedBranchName", pageData.getSelectedBranchName());
        model.addAttribute("branches", pageData.getBranches());
        model.addAttribute("courts", pageData.getCourts());
        model.addAttribute("timeSlots", pageData.getTimeSlots());
        model.addAttribute("slotGrid", pageData.getSlotGrid());
    }

    public void bindResultPage(Model model, BookingResultPageData pageData) {
        model.addAttribute("booking", pageData.getBooking());
        model.addAttribute("pageMode", pageData.getPageMode().getValue());
    }

    public void bindListPage(Model model, BookingListPageData pageData) {
        model.addAttribute("defaultBranchId", pageData.getDefaultBranchId());
        model.addAttribute("adminView", pageData.isAdminView());
        model.addAttribute("pageTitle", pageData.getPageTitle());
        model.addAttribute("pageDescription", pageData.getPageDescription());
        model.addAttribute("emptyMessage", pageData.getEmptyMessage());
        model.addAttribute("totalBookings", pageData.getTotalBookings());
        model.addAttribute("totalAmount", pageData.getTotalAmount());
        model.addAttribute("activeBookings", pageData.getActiveBookings());
        model.addAttribute("cancelledBookings", pageData.getCancelledBookings());
        model.addAttribute("bookings", pageData.getBookings());
    }
}
