package com.badminton.booking.dashboard.repository;

import com.badminton.booking.domain.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    interface BranchList {
        Long getBranchId();

        String getBranchName();

        String getBranchImage();

        String getAddress();

        BigDecimal getMinPrice();

        Integer getTotalReviews();

        Float getAvgRating();
    }

    @Query("""
                SELECT b.id, b.name
                FROM Branch b
                WHERE b.area.id = :areaId
                AND b.isDeleted = FALSE
            """)
    List<Object[]> getBranches(
            @Param("areaId") Integer areaId
    );

    @Query("""
                SELECT
                    b.id AS branchId,
                    b.name AS branchName,
                    (SELECT MAX(bi.imageUrl) FROM BranchImage bi WHERE bi.branch.id = b.id) AS branchImage,
                    b.address AS address,
                    (SELECT MIN(p.price) FROM Price p WHERE p.branch.id = b.id) AS minPrice,
                    b.totalReviews AS totalReviews,
                    b.averageRating AS avgRating
                FROM Branch b
                WHERE b.isDeleted = FALSE
                AND b.status = 'OPEN'
                AND EXISTS (SELECT 1 FROM BranchImage bi2 WHERE bi2.branch.id = b.id)
                AND EXISTS (SELECT 1 FROM Price p2 WHERE p2.branch.id = b.id)
                ORDER BY b.averageRating DESC, b.totalReviews DESC
                LIMIT 20 
            """)
    List<BranchList> getBranchList();
}
