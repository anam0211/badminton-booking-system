package com.badminton.booking.booking.service;

import com.badminton.booking.booking.dto.response.BookingCreatePageData;
import com.badminton.booking.booking.dto.response.SlotView;
import com.badminton.booking.booking.repository.BranchRepository;
import com.badminton.booking.booking.repository.CourtRepository;
import com.badminton.booking.booking.repository.TimeSlotRepository;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.domain.entity.TimeSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingPageService {

    private final BranchRepository branchRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingSlotGridService bookingSlotGridService;

    public BookingCreatePageData buildCreatePageData(Long userId, Long branchId, LocalDate playDate) {
        List<Court> courts = getCourtsByBranch(branchId);
        List<TimeSlot> timeSlots = timeSlotRepository.findAllByOrderByStartTimeAsc();
        Map<Long, Map<Integer, SlotView>> slotGrid =
                bookingSlotGridService.buildSlotGrid(courts, timeSlots, playDate);

        return BookingCreatePageData.builder()
                .userId(userId)
                .playDate(playDate)
                .branches(branchRepository.findAll())
                .courts(courts)
                .timeSlots(timeSlots)
                .slotGrid(slotGrid)
                .build();
    }

    private List<Court> getCourtsByBranch(Long branchId) {
        if (branchId == null) {
            return Collections.emptyList();
        }
        return courtRepository.findByBranch_IdOrderByNameAsc(branchId);
    }
}