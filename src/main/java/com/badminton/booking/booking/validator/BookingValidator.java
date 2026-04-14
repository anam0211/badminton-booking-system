package com.badminton.booking.booking.validator;

import com.badminton.booking.booking.dto.request.BookingRequest;
import com.badminton.booking.booking.dto.request.Slots;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class BookingValidator {

    public void validateCreate(BookingRequest request) {
        if (request == null || request.getBranchId() == null || request.getSlots() == null || request.getSlots().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        LocalDate today = LocalDate.now();
        Set<String> dedup = new HashSet<>();

        for (Slots s : request.getSlots()) {
            if (s.getCourtId() == null || s.getTimeSlotId() == null || s.getPlayDate() == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            

            if (s.getPlayDate().isBefore(today)) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            String key = s.getCourtId() + "|" + s.getTimeSlotId() + "|" + s.getPlayDate();
            if (!dedup.add(key)) {
                throw new AppException(ErrorCode.DUPLICATE_SLOT_IN_REQUEST);
            }
        }
    }
}