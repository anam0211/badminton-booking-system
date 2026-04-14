package com.badminton.booking.branch.repository;

import com.badminton.booking.domain.entity.BranchAmenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchAmenityRepository extends JpaRepository<BranchAmenity, Long> {

    List<BranchAmenity> findByBranchId(Long branchId);

    @Modifying
    @Query("DELETE FROM BranchAmenity ba WHERE ba.branch.id = :branchId")
    void deleteAllByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(ba) FROM BranchAmenity ba WHERE ba.branch.id = :branchId")
    Integer countAmenitiesByBranchId(@Param("branchId") Long branchId);
}
