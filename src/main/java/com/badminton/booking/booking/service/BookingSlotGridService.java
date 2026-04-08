package com.badminton.booking.booking.service;

import com.badminton.booking.booking.dto.response.SlotView;
import com.badminton.booking.booking.repository.BookingDetailRepository;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.domain.entity.BookingDetail;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.domain.entity.TimeSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingSlotGridService {

    private static final byte ACTIVE_KEY = 1;

    private final BookingDetailRepository bookingDetailRepository;
    private final BookingPricingService bookingPricingService;

    public Map<Long, Map<Integer, SlotView>> buildSlotGrid(List<Court> courts,
                                                           List<TimeSlot> timeSlots,
                                                           LocalDate playDate) {
        Map<Long, Map<Integer, SlotView>> grid = new HashMap<>();

        if (courts == null || courts.isEmpty() || timeSlots == null || timeSlots.isEmpty() || playDate == null) {
            return grid;
        }

        Long branchId = courts.get(0).getBranch().getId();
        Map<String, String> occupiedStatuses = buildOccupiedStatuses(branchId, playDate);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        for (Court court : courts) {
            Map<Integer, SlotView> row = new HashMap<>();
            for (TimeSlot timeSlot : timeSlots) {
                row.put(timeSlot.getId(), buildSlotView(court, timeSlot, playDate, today, now, occupiedStatuses));
            }
            grid.put(court.getId(), row);
        }

        return grid;
    }

    private SlotView buildSlotView(Court court,
                                   TimeSlot timeSlot,
                                   LocalDate playDate,
                                   LocalDate today,
                                   LocalTime now,
                                   Map<String, String> occupiedStatuses) {
        String status;
        BigDecimal price = null;

        String occupiedStatus = occupiedStatuses.get(buildSlotKey(court.getId(), timeSlot.getId(), playDate));
        if (occupiedStatus != null) {
            status = occupiedStatus;
        } else {
            try {
                price = bookingPricingService.calculate(court.getId(), timeSlot.getId());
                status = resolveStatus(playDate, today, now, timeSlot.getStartTime());
            } catch (AppException ex) {
                status = "NO_PRICE";
            }
        }

        return SlotView.builder()
                .courtId(court.getId())
                .courtName(court.getName())
                .timeSlotId(timeSlot.getId())
                .timeLabel(timeSlot.getStartTime().toString())
                .endTimeLabel(timeSlot.getEndTime().toString())
                .durationMinutes(calculateDurationMinutes(timeSlot))
                .price(price)
                .status(status)
                .build();
    }

    private Map<String, String> buildOccupiedStatuses(Long branchId, LocalDate playDate) {
        List<BookingDetail> bookingDetails = bookingDetailRepository.findActiveDetailsWithBookingByBranchAndPlayDate(
                branchId,
                playDate,
                ACTIVE_KEY
        );

        Map<String, String> occupiedStatuses = new HashMap<>();
        for (BookingDetail bookingDetail : bookingDetails) {
            occupiedStatuses.put(
                    buildSlotKey(
                            bookingDetail.getCourt().getId(),
                            bookingDetail.getTimeSlot().getId(),
                            bookingDetail.getPlayDate()
                    ),
                    "BOOKED"
            );
        }
        return occupiedStatuses;
    }

    private String buildSlotKey(Long courtId, Integer timeSlotId, LocalDate playDate) {
        return courtId + "|" + timeSlotId + "|" + playDate;
    }

    private String resolveStatus(LocalDate playDate,
                                 LocalDate today,
                                 LocalTime now,
                                 LocalTime startTime) {
        if (playDate.equals(today) && !startTime.isAfter(now)) {
            return "LOCKED";
        }
        return "AVAILABLE";
    }

    private long calculateDurationMinutes(TimeSlot timeSlot) {
        return Duration.between(timeSlot.getStartTime(), timeSlot.getEndTime()).toMinutes();
    }
}
