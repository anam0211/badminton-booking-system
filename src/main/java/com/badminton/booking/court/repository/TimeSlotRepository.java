package com.badminton.booking.court.repository;

import com.badminton.booking.domain.entity.TimeSlot;

import java.time.LocalTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {
    Optional<TimeSlot> findByStartTimeAndEndTime(LocalTime start, LocalTime end);
}