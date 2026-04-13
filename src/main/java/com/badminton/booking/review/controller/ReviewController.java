package com.badminton.booking.review.controller;

import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.domain.entity.Booking;
import com.badminton.booking.domain.entity.BookingDetail;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.booking.repository.BookingDetailRepository;
import com.badminton.booking.booking.repository.BookingRepository;
import com.badminton.booking.booking.repository.CourtRepository;
import com.badminton.booking.common.enums.BookingStatus;
import com.badminton.booking.review.dto.CreateReviewRequest;
import com.badminton.booking.review.dto.ReviewResponse;
import com.badminton.booking.review.service.ReviewService;
import com.badminton.booking.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final CourtRepository courtRepository;

    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam Long bookingId,
            @RequestParam Long courtId,
            Model model,
            RedirectAttributes redirectAttributes) {

        var currentUser = userService.getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền đánh giá booking này.");
            return "redirect:/";
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể đánh giá các booking đã hoàn thành.");
            return "redirect:/";
        }

        if (reviewService.hasUserReviewedBooking(bookingId, currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã đánh giá booking này rồi.");
            return "redirect:/";
        }

        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new AppException(ErrorCode.COURT_NOT_FOUND));

        List<BookingDetail> details = bookingDetailRepository.findByBooking_Id(bookingId);
        if (details.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết booking.");
            return "redirect:/";
        }

        CreateReviewRequest reviewRequest = new CreateReviewRequest();
        reviewRequest.setBookingId(bookingId);
        reviewRequest.setCourtId(courtId);
        reviewRequest.setBranchId(court.getBranch().getId());

        model.addAttribute("booking", booking);
        model.addAttribute("court", court);
        model.addAttribute("reviewRequest", reviewRequest);
        return "review/create";
    }

    @PostMapping("/create")
    public String createReview(
            @Valid @ModelAttribute("reviewRequest") CreateReviewRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
            return "redirect:/reviews/create?bookingId=" + request.getBookingId() + "&courtId=" + request.getCourtId();
        }

        try {
            var currentUser = userService.getCurrentUser();
            reviewService.createReview(request, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá!");
            return "redirect:/branches/" + request.getBranchId();
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reviews/create?bookingId=" + request.getBookingId() + "&courtId=" + request.getCourtId();
        }
    }

    @GetMapping("/branch/{branchId}")
    public String getBranchReviews(
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<ReviewResponse> reviews = reviewService.getReviewsByBranch(branchId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("branchId", branchId);
        return "review/list";
    }
}
