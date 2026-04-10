package com.badminton.booking.home.controller;

import com.badminton.booking.home.dto.response.BranchListResponse;
import com.badminton.booking.home.service.BranchListService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchListController {

    BranchListService branchListService;

    @GetMapping("/branches")
    public String showBranches(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "district", required = false) Integer districtId,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        if (priceRange != null && priceRange.matches("\\d+-\\d+")) {
            String[] prices = priceRange.split("-");
            minPrice = new BigDecimal(prices[0]);
            maxPrice = new BigDecimal(prices[1]);
        }

        Page<BranchListResponse> branchPage = branchListService.getBranchList(
                searchKeyword, districtId, minPrice, maxPrice, rating, page, size
        );

        model.addAttribute("branchList", branchPage.getContent());
        model.addAttribute("branchPage", branchPage);
        model.addAttribute("keyword", searchKeyword);
        model.addAttribute("district", districtId);
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("rating", rating);

        return "branch/branch-list";
    }
}