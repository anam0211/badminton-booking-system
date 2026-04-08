package com.badminton.booking.booking.mapper;

import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.domain.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toDto(Booking entity) {
        if (entity == null) {
            return null;
        }

        BookingResponse response = new BookingResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        response.setUserFullName(entity.getUser() != null ? entity.getUser().getFullName() : null);
        response.setUserEmail(entity.getUser() != null ? entity.getUser().getEmail() : null);
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setPaymentStatus(entity.getPaymentStatus() != null ? entity.getPaymentStatus().name() : null);
        response.setTotalAmount(entity.getTotalAmount());
        response.setNote(entity.getNote());
        response.setBookingDate(entity.getBookingDate());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
