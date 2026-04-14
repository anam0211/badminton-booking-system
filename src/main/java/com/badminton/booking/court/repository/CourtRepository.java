package com.badminton.booking.court.repository;

import com.badminton.booking.domain.entity.Court;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {

    List<Court> findByBranchIdAndIsDeletedFalse(Long branchId);

    @EntityGraph(attributePaths = {"branch"})
    Court findCourtByIdAndIsDeletedFalse(Long id);
}