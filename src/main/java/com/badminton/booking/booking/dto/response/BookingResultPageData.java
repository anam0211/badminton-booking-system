package com.badminton.booking.booking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingResultPageData {
    private BookingResponse booking;
    private BookingPageMode pageMode;
}
