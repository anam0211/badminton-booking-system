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
    public void handleBookingSuccess(BookingSuccessEvent event){
        Long userId = event.getUserId();
        String title = "Đặt sân thành công";
        String actionUrl = "/api/bookings/my";
        String content = String.format("Bạn đã đặt %s vào ngày %s, ca %s thành công", event.getCourtName(), event.getPlayDate(), event.getTimeSlot());
        notificationService.createNotification(userId, title, content, actionUrl);
    }

    @EventListener
    public void handleBookingCancel(BookingCancelEvent event){
        Long userId = event.getUserId();
        String title = "Hủy lịch đặt sân";
        String actionUrl = "/api/bookings/my";
        String content = String.format("Bạn đã hủy lịch %s vào ngày %s, ca %s thành công", event.getCourtName(), event.getPlayDate(), event.getTimeSlot());
        notificationService.createNotification(userId, title, content, actionUrl);
    }

    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event){
        Long userId = event.getUserId();
        String title = "Thanh toán thành công";
        String actionUrl = "payments/my";
        String content = String.format("Hệ thống đã ghi nhận khoản thanh toán %s cho đơn hàng %s. Cảm ơn bạn đã sử dụng dịch vụ", event.getAmount(), event.getBookingId());
        notificationService.createNotification(userId, title, content, actionUrl);
    }
}
