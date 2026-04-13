package com.badminton.booking.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookingSuccessEvent {
    private Long userId;
    private Long bookingId;
    private String courtName;
    private String playDate;
    private String timeSlot;
}
