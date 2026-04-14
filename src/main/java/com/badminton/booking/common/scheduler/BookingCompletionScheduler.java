package com.badminton.booking.common.scheduler;

import com.badminton.booking.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingCompletionScheduler {

    private final BookingService bookingService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void completeFinishedBookings() {
        int completedCount = bookingService.completeFinishedBookings();
        if (completedCount > 0) {
            log.info("Auto-completed {} finished booking(s).", completedCount);
        }
    }
}
