package com.badminton.booking.booking.mapper;

import com.badminton.booking.booking.dto.response.BookingDetailResponse;
import com.badminton.booking.domain.entity.BookingDetail;
import org.springframework.stereotype.Component;

@Component
public class BookingDetailMapper {

    public BookingDetailResponse toDto(BookingDetail entity) {
        if (entity == null) {
            return null;
        }

        BookingDetailResponse response = new BookingDetailResponse();
        response.setBookingDetailId(entity.getId());
        response.setBranchId(entity.getCourt() != null && entity.getCourt().getBranch() != null
                ? entity.getCourt().getBranch().getId()
                : null);
        response.setBranchName(entity.getCourt() != null && entity.getCourt().getBranch() != null
                ? entity.getCourt().getBranch().getName()
                : null);
        response.setCourtId(entity.getCourt() != null ? entity.getCourt().getId() : null);
        response.setCourtName(entity.getCourt() != null ? entity.getCourt().getName() : null);
        response.setTimeSlotId(entity.getTimeSlot() != null ? entity.getTimeSlot().getId() : null);
        response.setStartTime(entity.getTimeSlot() != null && entity.getTimeSlot().getStartTime() != null
                ? entity.getTimeSlot().getStartTime().toString()
                : null);
        response.setEndTime(entity.getTimeSlot() != null && entity.getTimeSlot().getEndTime() != null
                ? entity.getTimeSlot().getEndTime().toString()
                : null);
        response.setPlayDate(entity.getPlayDate());
        response.setPrice(entity.getPrice());
        response.setActive(entity.getActiveKey() != null ? entity.getActiveKey().intValue() : null);
        return response;
    }
}
