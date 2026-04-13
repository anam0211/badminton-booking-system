package com.badminton.booking.branch.controller;

import com.badminton.booking.branch.dto.response.BranchResponse;
import com.badminton.booking.branch.service.BranchService;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.review.dto.ReviewResponse;
import com.badminton.booking.review.service.ReviewService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/branches")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchController {

    BranchService branchService;
    ReviewService reviewService;

    @GetMapping("/{id}")
    public String branchDetail(@PathVariable Long id, Model model) {
        try {
            BranchResponse branch = branchService.getBranchDetail(id);
            List<ReviewResponse> reviews = reviewService.getReviewsByBranch(id);
            model.addAttribute("branch", branch);
            model.addAttribute("reviews", reviews);
            return "branch/detail";
        } catch (AppException ex) {
            return "redirect:/branches";
        }
    }
}
