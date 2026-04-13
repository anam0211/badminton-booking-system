package com.badminton.booking.booking.support;

import com.badminton.booking.booking.dto.request.BookingRequest;
import com.badminton.booking.booking.dto.request.Slots;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.ArrayList;

@Component
public class BookingFormSupport {

    public void ensureBookingRequest(Model model, Long branchId) {
        if (model.containsAttribute("bookingRequest")) {
            return;
        }

        BookingRequest request = new BookingRequest();
        request.setBranchId(branchId);
        request.setSlots(new ArrayList<>());
        model.addAttribute("bookingRequest", request);
    }

    public void normalize(BookingRequest request) {
        if (request.getSlots() == null) {
            request.setSlots(new ArrayList<>());
        }
    }

    public LocalDate resolvePlayDate(BookingRequest request) {
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
