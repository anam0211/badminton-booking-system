package com.badminton.booking.booking.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badminton.booking.domain.entity.Court;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
     List<Court> findByBranch_IdOrderByNameAsc(Long branchId);

     long countByBranch_IdAndIsDeletedFalse(Long branchId);
}
