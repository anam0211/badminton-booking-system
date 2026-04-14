package com.badminton.booking.booking.service;

import com.badminton.booking.domain.entity.Booking;
import com.badminton.booking.domain.entity.BookingDetail;
import com.badminton.booking.domain.entity.TimeSlot;
import com.badminton.booking.notification.event.BookingCancelEvent;
import com.badminton.booking.notification.event.BookingSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventService {

    private final ApplicationEventPublisher eventPublisher;

    public void publishCreatedSafely(Booking booking, List<BookingDetail> bookingDetails) {
        publishSafely(
                booking,
                bookingDetails,
                "tạo",
                detail -> new BookingSuccessEvent(
                        booking.getUser().getId(),
                        booking.getId(),
                        detail.getCourt().getName(),
                        detail.getPlayDate().toString(),
                        formatTimeSlot(detail.getTimeSlot())
                )
        );
    }

    public void publishCancelledSafely(Booking booking, List<BookingDetail> bookingDetails) {
        publishSafely(
                booking,
                bookingDetails,
                "hủy",
                detail -> new BookingCancelEvent(
                        booking.getUser().getId(),
                        booking.getId(),
                        detail.getCourt().getName(),
                        detail.getPlayDate().toString(),
                        formatTimeSlot(detail.getTimeSlot())
                )
        );
    }

    private void publishSafely(Booking booking,
                               List<BookingDetail> bookingDetails,
                               String action,
                               Function<BookingDetail, Object> eventFactory) {
        if (booking == null || booking.getUser() == null || booking.getUser().getId() == null) {
            return;
        }

        try {
            for (BookingDetail bookingDetail : bookingDetails) {
                eventPublisher.publishEvent(eventFactory.apply(bookingDetail));
            }
        } catch (RuntimeException ex) {
            log.warn("Không thể gửi thông báo sau khi {} booking {}", action, booking.getId(), ex);
        }
    }

    private String formatTimeSlot(TimeSlot timeSlot) {
        if (timeSlot == null) {
            return "";
        }
        return timeSlot.getStartTime() + " - " + timeSlot.getEndTime();
    }
}
