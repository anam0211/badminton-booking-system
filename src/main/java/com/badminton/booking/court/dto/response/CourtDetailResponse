package com.badminton.booking.court.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CourtDetailResponse {

    private Long courtId;
    private String courtName;
    private String courtType;

    private Long branchId;
    private String branchName;
    private String address;

    private List<PriceItem> prices;
    private List<TimeSlotItem> timeSlots;

    @Getter
    @Setter
    @Builder
    public static class PriceItem {
        private String courtType;
        private BigDecimal price;
        private String slotName;
    }

    @Getter
    @Setter
    @Builder
    public static class TimeSlotItem {
        private Integer id;
        private String slotName;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}