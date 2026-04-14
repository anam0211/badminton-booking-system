package com.badminton.booking.booking.repository;

import com.badminton.booking.domain.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingPriceRepository extends JpaRepository<Price, Long> {

    Optional<Price> findByBranch_IdAndTimeSlot_IdAndCourtTypeIgnoreCase(
            Long branchId,
            Integer timeSlotId,
            String courtType
    );

    Optional<Price> findByBranch_IdAndTimeSlot_IdAndCourtTypeIsNull(
            Long branchId,
            Integer timeSlotId
    );
}