
package com.badminton.booking.court.service;

import com.badminton.booking.domain.entity.TimeSlot;
import com.badminton.booking.court.repository.CourtTimeSlotRepository;
import com.badminton.booking.court.repository.CourtTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final CourtTimeSlotRepository repo;

    public List<TimeSlot> getAll() {
        return repo.findAll();
    }

    public TimeSlot save(TimeSlot s) {
        return repo.save(s);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}
