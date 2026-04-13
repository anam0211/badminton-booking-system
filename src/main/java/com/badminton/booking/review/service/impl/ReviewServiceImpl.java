package com.badminton.booking.review.service.impl;

import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.domain.entity.*;
import com.badminton.booking.domain.repository.UserRepository;
import com.badminton.booking.booking.repository.BookingDetailRepository;
import com.badminton.booking.booking.repository.BookingRepository;
import com.badminton.booking.booking.repository.CourtRepository;
import com.badminton.booking.review.dto.CreateReviewRequest;
import com.badminton.booking.review.dto.ReviewResponse;
import com.badminton.booking.review.repository.ReviewRepository;
import com.badminton.booking.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final com.badminton.booking.dashboard.repository.BranchRepository branchRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_BE_REVIEWED);
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_BE_REVIEWED);
        }

        if (reviewRepository.existsByBookingIdAndUserId(request.getBookingId(), userId)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new AppException(ErrorCode.COURT_NOT_FOUND));

        List<BookingDetail> details = bookingDetailRepository.findByBooking_Id(request.getBookingId());
        if (details.isEmpty() || !details.stream()
                .anyMatch(d -> d.getCourt().getBranch().getId().equals(court.getBranch().getId()))) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_BE_REVIEWED);
        }

        Review review = Review.builder()
                .user(user)
                .branch(court.getBranch())
                .court(court)
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);

        recalculateBranchRating(court.getBranch().getId());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByBranch(Long branchId) {
        return reviewRepository.findByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedBooking(Long bookingId, Long userId) {
        return reviewRepository.existsByBookingIdAndUserId(bookingId, userId);
    }

    private void recalculateBranchRating(Long branchId) {
        Float avgRating = reviewRepository.avgRatingByBranchId(branchId);
        if (avgRating == null) {
            avgRating = 0f;
        }
        long total = reviewRepository.countByBranchId(branchId);

        branchRepository.findById(branchId).ifPresent(branch -> {
            branch.setAverageRating(Math.round(avgRating * 100) / 100f);
            branch.setTotalReviews((int) total);
            branchRepository.save(branch);
        });
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .courtId(review.getCourt().getId())
                .courtName(review.getCourt().getName())
                .branchId(review.getBranch().getId())
                .branchName(review.getBranch().getName())
                .userId(review.getUser().getId())
                .userFullName(review.getUser().getFullName())
                .userAvatarUrl(review.getUser().getAvatarUrl())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
