package com.badminton.booking.booking.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingDetailResponse {
    private Long bookingDetailId;
    private Long courtId;
    private String courtName;
    private Integer timeSlotId;
    private String startTime;
    private String endTime;
    private LocalDate playDate;
    private BigDecimal price;
    private Integer active;
}