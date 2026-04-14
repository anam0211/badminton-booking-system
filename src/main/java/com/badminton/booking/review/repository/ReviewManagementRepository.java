package com.badminton.booking.review.repository;

import com.badminton.booking.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewManagementRepository extends JpaRepository<Review, Long> {

    List<Review> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    Page<Review> findByBranchIdOrderByCreatedAtDesc(Long branchId, Pageable pageable);

    Optional<Review> findByBookingIdAndUserId(Long bookingId, Long userId);

    boolean existsByBookingIdAndUserId(Long bookingId, Long userId);

    long countByBranchId(Long branchId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.branch.id = :branchId")
    Float avgRatingByBranchId(@Param("branchId") Long branchId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
}
