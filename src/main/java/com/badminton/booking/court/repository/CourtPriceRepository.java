package com.badminton.booking.court.repository;

import com.badminton.booking.domain.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface CourtPriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByBranchIdAndTimeSlotId(Long branchId, Integer timeSlotId);
    List<Price> findByBranchId(Long branchId);

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM Price p
    WHERE p.branch.id = :branchId
    AND p.timeSlot.id = :timeSlotId
    """)
    void deleteByBranchIdAndTimeSlotId(Long branchId, Integer timeSlotId);

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM Price p
        WHERE p.branch.id = :branchId
        AND p.timeSlot.startTime = :start
        AND p.timeSlot.endTime = :end
    """)
    boolean existsByBranchIdAndStartEnd(Long branchId, LocalTime start, LocalTime end);

}

