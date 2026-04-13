package com.badminton.booking.dashboard.repository;

import com.badminton.booking.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    interface ReviewSummary {
        Integer getTotalReviews();

        Float getAvgRating();
    }

    @Query("""
                SELECT COUNT(r.id) as totalReviews, COALESCE(AVG(r.rating), 0.0) as avgRating
                FROM Review r  
                WHERE r.branch.id = :branchId
                AND MONTH(r.createdAt) = :month
                AND YEAR(r.createdAt) = :year             
            """)
    ReviewSummary getReviewSummaryByMonth(
            @Param("branchId") Long branchId,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("""
                SELECT COUNT(r.id) as totalReviews, COALESCE(AVG(r.rating), 0.0) as avgRating
                FROM Review r  
                WHERE r.branch.id = :branchId
                AND YEAR(r.createdAt) = :year             
            """)
    ReviewSummary getReviewSummaryByYear(
            @Param("branchId") Long branchId,
            @Param("year") int year
    );
}
