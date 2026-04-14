package com.badminton.booking.dashboard.repository;

import com.badminton.booking.common.enums.BranchStatus;
import com.badminton.booking.domain.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    interface BranchList {
        Long getBranchId();
        String getBranchName();
        String getBranchImage();
        String getAddress();
        String getAreaName();
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
                    b.area.name AS areaName,
                    (SELECT MIN(p.price) FROM Price p WHERE p.branch.id = b.id) AS minPrice,
                    b.totalReviews AS totalReviews,
                    b.averageRating AS avgRating
                FROM Branch b
                WHERE b.isDeleted = FALSE
                AND b.status = 'OPEN'
                AND EXISTS (SELECT 1 FROM BranchImage bi2 WHERE bi2.branch.id = b.id)
                AND EXISTS (SELECT 1 FROM Price p2 WHERE p2.branch.id = b.id)
                ORDER BY b.totalReviews DESC, b.averageRating DESC
            """)
    List<BranchList> getFeatureBranches(Pageable pageable);

    @Query(value = """
            SELECT
                b.id AS branchId,
                b.name AS branchName,
                (SELECT MAX(bi.imageUrl) FROM BranchImage bi WHERE bi.branch.id = b.id) AS branchImage,
                b.address AS address,
                b.area.name AS areaName,
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

    @EntityGraph(attributePaths = {"area", "branchAmenities", "branchImages"})
    @Query("SELECT b FROM Branch b WHERE b.id = :id AND b.isDeleted = false")
    Optional<Branch> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b FROM Branch b JOIN FETCH b.area WHERE b.isDeleted = false ORDER BY b.name ASC")
    List<Branch> findAllWithArea();

    @Query("""
            SELECT b FROM Branch b
            JOIN FETCH b.area
            WHERE b.isDeleted = false
            AND (:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:areaId IS NULL OR b.area.id = :areaId)
            AND (:status IS NULL OR b.status = :status)
            ORDER BY b.name ASC
            """)
    Page<Branch> searchBranches(
            @Param("keyword") String keyword,
            @Param("areaId") Integer areaId,
            @Param("status") BranchStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT MIN(p.price) FROM Price p
            WHERE p.branch.id = :branchId
            """)
    BigDecimal findMinPriceByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(c) FROM Court c WHERE c.branch.id = :branchId AND c.isDeleted = false")
    Integer countCourtsByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(bi) FROM BranchImage bi WHERE bi.branch.id = :branchId")
    Integer countImagesByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(ba) FROM BranchAmenity ba WHERE ba.branch.id = :branchId")
    Integer countAmenitiesByBranchId(@Param("branchId") Long branchId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
