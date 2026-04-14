package com.badminton.booking.branch.repository;

import com.badminton.booking.domain.entity.BranchImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchImageRepository extends JpaRepository<BranchImage, Long> {

    List<BranchImage> findByBranchIdOrderByIdAsc(Long branchId);

    List<BranchImage> findAllByBranchId(Long branchId);

    @Modifying
    @Query("DELETE FROM BranchImage bi WHERE bi.branch.id = :branchId")
    void deleteAllByBranchId(@Param("branchId") Long branchId);

    Integer countByBranchId(Long branchId);
}
