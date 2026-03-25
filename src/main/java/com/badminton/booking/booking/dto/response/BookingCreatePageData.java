
package com.badminton.booking.booking.dto.response;

import com.badminton.booking.domain.entity.Branch;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.domain.entity.TimeSlot;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class BookingCreatePageData {
    private Long userId;
    private LocalDate playDate;
    private List<Branch> branches;
    private List<Court> courts;
    private List<TimeSlot> timeSlots;
    private Map<Long, Map<Integer, SlotView>> slotGrid;
}