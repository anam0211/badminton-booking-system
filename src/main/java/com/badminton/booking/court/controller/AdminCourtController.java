package com.badminton.booking.court.controller;

import com.badminton.booking.court.repository.*;
import com.badminton.booking.domain.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/courts")
@RequiredArgsConstructor
public class AdminCourtController {

    private final CourtRepository courtRepo;
    private final PriceRepository priceRepo;
    private final TimeSlotRepository timeSlotRepo;

    // ================= LIST =================
    @GetMapping
    public String list(@PathVariable Long branchId, Model model) {
  
        var courts = courtRepo.findByBranchIdAndIsDeletedFalse(branchId);
        var prices = priceRepo.findByBranchId(branchId);
        var timeslots = timeSlotRepo.findAll();

        model.addAttribute("courts", courts);
        model.addAttribute("prices", prices);
        model.addAttribute("timeslots", timeslots);
        model.addAttribute("branchId", branchId);

        return "admin/court/list";

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

        return "redirect:/admin/courts?branchId=" + branchId;
    }


    @GetMapping("/edit/{id}")
public String edit(@PathVariable Long id, Model model) {

    Court court = courtRepo.findById(id).orElseThrow();

    Long branchId = court.getBranch().getId();

    // GLOBAL PRICE (KHÔNG FILTER COURT TYPE)
    var prices = priceRepo.findByBranchId(branchId);

    var timeslots = timeSlotRepo.findAll().stream()
            .map(t -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", t.getId());
                m.put("slotName", t.getSlotName());
                m.put("startTime", t.getStartTime());
                m.put("endTime", t.getEndTime());
                return m;
            })
            .toList();

    model.addAttribute("court", court);
    model.addAttribute("prices", prices);
    model.addAttribute("timeslots", timeslots);
    model.addAttribute("branchId", branchId);

    return "admin/court/edit";
}

   @Transactional
@PostMapping("/update/{id}")
public String update(@PathVariable Long id,
                     @RequestParam(required = false) List<Integer> timeSlotIds,
                     @RequestParam(required = false) List<String> startTimes,
                     @RequestParam(required = false) List<String> endTimes,
                     @RequestParam(required = false) List<BigDecimal> prices,
                     @ModelAttribute Court req) {

    Court c = courtRepo.findById(id).orElseThrow();
    Long branchId = c.getBranch().getId();

    c.setName(req.getName());
    c.setType(req.getType());
    c.setStatus(req.getStatus());
    courtRepo.save(c);

    // delete old prices
    List<Price> old = priceRepo.findByBranchId(branchId);
    priceRepo.deleteAll(old);

    if (prices == null) return "redirect:/admin/courts?branchId=" + branchId;

    int size = prices.size();

    for (int i = 0; i < size; i++) {

        TimeSlot t;

        Integer slotId = (timeSlotIds != null && i < timeSlotIds.size())
                ? timeSlotIds.get(i)
                : null;

        String start = startTimes.get(i);
        String end = endTimes.get(i);

        // 🟢 CASE 1: UPDATE EXISTING TIME SLOT
        if (slotId != null && slotId > 0) {
            t = timeSlotRepo.findById(slotId)
                    .orElseThrow();

            t.setStartTime(java.time.LocalTime.parse(start));
            t.setEndTime(java.time.LocalTime.parse(end));
            t.setSlotName(start + "-" + end);

            t = timeSlotRepo.save(t);
        }
        // 🟢 CASE 2: CREATE NEW TIME SLOT
        else {
            t = new TimeSlot();
            t.setStartTime(java.time.LocalTime.parse(start));
            t.setEndTime(java.time.LocalTime.parse(end));
            t.setSlotName(start + "-" + end);

            t = timeSlotRepo.save(t);
        }

        Price p = new Price();
        p.setBranch(c.getBranch());
        p.setTimeSlot(t);
        p.setPrice(prices.get(i));

        priceRepo.save(p);
    }

    return "redirect:/admin/courts?branchId=" + branchId;
}
                


    // ================= DELETE =================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        var c = courtRepo.findById(id).orElseThrow();

        c.setIsDeleted(true);
        courtRepo.save(c);

        return "redirect:/admin/courts?branchId=" + c.getBranch().getId();
    }
}
