package com.badminton.booking.court.repository;

import com.badminton.booking.domain.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByBranchId(Long branchId);


    @Modifying
    @Transactional
    @Query("""
    DELETE FROM Price p
    WHERE p.branch.id = :branchId
    AND p.timeSlot.id = :timeSlotId
    """)
    void deleteByBranchIdAndTimeSlotId(Long branchId, Integer timeSlotId);


}

