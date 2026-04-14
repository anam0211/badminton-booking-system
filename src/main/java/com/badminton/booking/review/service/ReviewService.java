package com.badminton.booking.review.service;

import com.badminton.booking.review.dto.CreateReviewRequest;
import com.badminton.booking.review.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(CreateReviewRequest request, Long userId);

    List<ReviewResponse> getReviewsByBranch(Long branchId);

    List<ReviewResponse> getReviewsByUser(Long userId);

    boolean hasUserReviewedBooking(Long bookingId, Long userId);
}
