package com.badminton.booking.booking.mapper;

import com.badminton.booking.booking.dto.response.BookingResponse;
import com.badminton.booking.domain.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingResponse toDto(Booking entity);
}