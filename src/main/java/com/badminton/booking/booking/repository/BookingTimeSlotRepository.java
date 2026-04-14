
package com.badminton.booking.booking.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badminton.booking.domain.entity.TimeSlot;

@Repository
public interface BookingTimeSlotRepository extends JpaRepository<TimeSlot, Integer> {
     List<TimeSlot> findAllByOrderByStartTimeAsc();
  
}