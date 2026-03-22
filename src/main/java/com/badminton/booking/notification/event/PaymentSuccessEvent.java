package com.badminton.booking.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent {
    private Long userId;
    private String bookingId;
    private String amount;
}
