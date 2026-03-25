package com.badminton.booking.booking.mapper;

import com.badminton.booking.booking.dto.response.BookingDetailResponse;
import com.badminton.booking.domain.entity.BookingDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingDetailMapper {

    @Mapping(source = "id", target = "bookingDetailId")
    @Mapping(source = "court.id", target = "courtId")
    @Mapping(source = "court.name", target = "courtName")
    @Mapping(source = "timeSlot.id", target = "timeSlotId")
    @Mapping(source = "timeSlot.startTime", target = "startTime")
    @Mapping(source = "timeSlot.endTime", target = "endTime")
    @Mapping(source = "activeKey", target = "active")
    BookingDetailResponse toDto(BookingDetail entity);
}