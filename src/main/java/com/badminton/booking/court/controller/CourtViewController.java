package com.badminton.booking.court.controller;

import com.badminton.booking.court.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import com.badminton.booking.court.repository.PriceRepository;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class CourtViewController {

    private final CourtRepository repo;
    private final PriceRepository priceRepository;

    @GetMapping("/branches/{branchId}/courts")
  //  public String list(@PathVariable Long branchId, Model model)
    public String list(Model model) {
        Long branchId = 2L;
        var courts = repo.findByBranchIdAndIsDeletedFalse(branchId);

        model.addAttribute("courts", courts);
        model.addAttribute("branchId", branchId);

        return "court/list";
    }

    @GetMapping("/courts/{id}")
    public String detail(@PathVariable Long id, Model model) {

        var court = repo.findById(id).orElseThrow();

        Long branchId = court.getBranch().getId();

        var prices = priceRepository.findByBranchId(branchId)
                .stream();

        model.addAttribute("court", court);
        model.addAttribute("prices", prices);

        return "court/detail";
    }


}