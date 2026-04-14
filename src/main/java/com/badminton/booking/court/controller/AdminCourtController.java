package com.badminton.booking.court.controller;

import com.badminton.booking.court.repository.*;
import com.badminton.booking.domain.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/courts")
@RequiredArgsConstructor
    
    public class AdminCourtController {

        private final CourtCourtRepository courtRepo;
        private final CourtPriceRepository priceRepo;
        private final CourtTimeSlotRepository timeSlotRepo;
        private final CourtUserRepository userRepo;



    @GetMapping
    public String list(Model model) {

        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof UserDetails userDetails) {

            String email = userDetails.getUsername();

            User user = userRepo.findByEmail(email).orElseThrow();

            Branch branch = user.getManagedBranch();

            List<Branch> branches = List.of(branch);

            Map<Long, List<Court>> courtMap = new HashMap<>();

            for (Branch b : branches) {
                courtMap.put(b.getId(),
                    courtRepo.findByBranchIdAndIsDeletedFalse(b.getId()));
            }

            model.addAttribute("branches", branches);
            model.addAttribute("courtMap", courtMap);

            return "admin/court/list";
        }

        return "redirect:/login";
    }

    @GetMapping("/create")
    public String createPage(@RequestParam Long branchId, Model model) {

        var timeslots = timeSlotRepo.findAll().stream()
                    .map(t -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", t.getId());
                        m.put("slotName", t.getSlotName());
                        return m;
                    })
                    .toList();

        model.addAttribute("court", new Court());
        model.addAttribute("timeslots", timeslots);
        model.addAttribute("branchId", branchId);

        return "admin/court/create";
    }

    
    @PostMapping
    public String create(@ModelAttribute Court court,
                        @RequestParam Long branchId,
                        @RequestParam(required = false) List<Integer> timeSlotIds,
                        @RequestParam(required = false) List<BigDecimal> prices) {

        Branch b = new Branch();
        b.setId(branchId);
        court.setBranch(b);

        courtRepo.save(court);

        if (timeSlotIds == null || prices == null) {
            return "redirect:/admin/courts?branchId=" + branchId;
        }

        int size = Math.min(timeSlotIds.size(), prices.size());

        for (int i = 0; i < size; i++) {

            Price p = new Price();
            p.setBranch(b);
            p.setCourtType(court.getType());
            p.setPrice(prices.get(i));

            TimeSlot t = new TimeSlot();
            t.setId(timeSlotIds.get(i));

            p.setTimeSlot(t);

            priceRepo.save(p);
        }

        return "redirect:/admin/courts";
    }


    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {

        Court court = courtRepo.findById(id).orElseThrow();

        model.addAttribute("court", court);

        return "admin/court/edit";
    }


    @Transactional
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                        @ModelAttribute Court req) {

        Court c = courtRepo.findById(id).orElseThrow();

        c.setName(req.getName());
        c.setType(req.getType());
        c.setStatus(req.getStatus());

        courtRepo.save(c);

        return "redirect:/admin/courts";
    }


    @GetMapping("/prices/edit")
    public String editPrice(Model model) {

        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = userRepo.findByEmail(((UserDetails) principal).getUsername())
                .orElseThrow();

        Long branchId = user.getManagedBranch().getId();

        var prices = priceRepo.findByBranchId(branchId);

        model.addAttribute("prices", prices);
        model.addAttribute("branchId", branchId);

        return "admin/price/edit";
    }



    @Transactional
    @PostMapping("/prices/update")
    public String updatePrices(@RequestParam List<String> startTimes,
                            @RequestParam List<String> endTimes,
                            @RequestParam List<BigDecimal> prices) {

        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = userRepo.findByEmail(((UserDetails) principal).getUsername())
                .orElseThrow();

        Branch branch = user.getManagedBranch();

        // Xóa toàn bộ price cũ
        priceRepo.deleteAll(priceRepo.findByBranchId(branch.getId()));

        
        int size = Math.min(prices.size(),
            Math.min(startTimes.size(), endTimes.size()));

            for (int i = 0; i < size; i++) {

            TimeSlot t = new TimeSlot();
            t.setStartTime(LocalTime.parse(startTimes.get(i)));
            t.setEndTime(LocalTime.parse(endTimes.get(i)));
            t.setSlotName(startTimes.get(i) + "-" + endTimes.get(i));

            t = timeSlotRepo.save(t); 

            Price p = new Price();
            p.setBranch(branch);
            p.setTimeSlot(t);
            p.setPrice(prices.get(i));

            priceRepo.save(p);
        }

        return "redirect:/admin/courts";
    }
                
    



    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        var c = courtRepo.findById(id).orElseThrow();

        c.setIsDeleted(true);
        courtRepo.save(c);

        return "redirect:/admin/courts";  
    }



}