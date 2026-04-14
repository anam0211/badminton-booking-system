package com.badminton.booking.booking.dto.response;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
@Getter
@Builder
public class SlotView {
    private Long courtId;
    private String courtName;
    private Integer timeSlotId;
    private String timeLabel;
    private Long durationMinutes;
    private BigDecimal price;
    private String status; // AVAILABLE, BOOKED, LOCKED
    private String endTimeLabel;
}
