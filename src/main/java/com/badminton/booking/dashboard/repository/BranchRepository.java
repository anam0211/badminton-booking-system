package com.badminton.booking.dashboard.repository;

import com.badminton.booking.domain.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            """)
    List<BranchList> getFeatureBranches(Pageable pageable);

    @Query(value = """
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
            AND (:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:districtId IS NULL OR b.area.id = :districtId)
            AND (:rating IS NULL OR b.averageRating >= :rating)
            AND (:minPrice IS NULL OR (SELECT MIN(p.price) FROM Price p WHERE p.branch.id = b.id) >= :minPrice)
            AND (:maxPrice IS NULL OR (SELECT MIN(p.price) FROM Price p WHERE p.branch.id = b.id) <= :maxPrice)
            ORDER BY b.averageRating DESC, b.totalReviews DESC
            """,
            countQuery = """
            SELECT count(b) FROM Branch b 
            WHERE b.isDeleted = FALSE AND b.status = 'OPEN'
            AND EXISTS (SELECT 1 FROM BranchImage bi2 WHERE bi2.branch.id = b.id)
            AND EXISTS (SELECT 1 FROM Price p2 WHERE p2.branch.id = b.id)
            AND (:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:districtId IS NULL OR b.area.id = :districtId)
            AND (:rating IS NULL OR b.averageRating >= :rating)
            AND (:minPrice IS NULL OR (SELECT MIN(p.price) FROM Price p WHERE p.branch.id = b.id) >= :minPrice)
            AND (:maxPrice IS NULL OR (SELECT MIN(p.price) FROM Price p WHERE p.branch.id = b.id) <= :maxPrice)
            """)
    Page<BranchList> getBranchList(
            @Param("keyword") String keyword,
            @Param("districtId") Integer districtId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("rating") Integer rating,
            Pageable pageable
    );
}
