package com.badminton.booking.notification.listener;

import com.badminton.booking.notification.event.BookingCancelEvent;
import com.badminton.booking.notification.event.BookingSuccessEvent;
import com.badminton.booking.notification.event.PaymentSuccessEvent;
import com.badminton.booking.notification.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationListener {
    NotificationService notificationService;

    @EventListener
    public void handleBookingSuccess(BookingSuccessEvent event) {
        Long userId = event.getUserId();
        String title = "\u0110\u1eb7t s\u00e2n th\u00e0nh c\u00f4ng";
        String actionUrl = "/bookings/history";
        String content = String.format("B\u1ea1n \u0111\u00e3 \u0111\u1eb7t %s v\u00e0o ng\u00e0y %s, ca %s th\u00e0nh c\u00f4ng",
                event.getCourtName(), event.getPlayDate(), event.getTimeSlot());
        notificationService.createNotification(userId, title, content, actionUrl);
    }

    @EventListener
    public void handleBookingCancel(BookingCancelEvent event) {
        Long userId = event.getUserId();
        String title = "H\u1ee7y l\u1ecbch \u0111\u1eb7t s\u00e2n";
        String actionUrl = "/bookings/history";
        String content = String.format("B\u1ea1n \u0111\u00e3 h\u1ee7y l\u1ecbch %s v\u00e0o ng\u00e0y %s, ca %s th\u00e0nh c\u00f4ng",
                event.getCourtName(), event.getPlayDate(), event.getTimeSlot());
        notificationService.createNotification(userId, title, content, actionUrl);
    }

    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        Long userId = event.getUserId();
        String title = "Thanh to\u00e1n th\u00e0nh c\u00f4ng";
        String actionUrl = "/bookings/history";
        String content = String.format("H\u1ec7 th\u1ed1ng \u0111\u00e3 ghi nh\u1eadn kho\u1ea3n thanh to\u00e1n %s cho booking %s.",
                event.getAmount(), event.getBookingId());
        notificationService.createNotification(userId, title, content, actionUrl);
    }
}
