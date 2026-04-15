package com.badminton.booking.court.controller;

import com.badminton.booking.court.repository.CourtCourtRepository;
import com.badminton.booking.court.repository.CourtPriceRepository;
import com.badminton.booking.court.repository.CourtTimeSlotRepository;
import com.badminton.booking.court.repository.CourtUserRepository;
import com.badminton.booking.domain.entity.Branch;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.domain.entity.Price;
import com.badminton.booking.domain.entity.TimeSlot;
import com.badminton.booking.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                courtMap.put(b.getId(), courtRepo.findByBranchIdAndIsDeletedFalse(b.getId()));
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
                .sorted(Comparator.comparing(TimeSlot::getStartTime))
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", t.getId());
                    m.put("slotName", t.getSlotName());
                    m.put("startTime", t.getStartTime());
                    m.put("endTime", t.getEndTime());
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
        Long branchId = court.getBranch().getId();
        String courtType = normalizeCourtType(court.getType());

        var prices = priceRepo.findByBranchIdAndCourtTypeIgnoreCaseOrderByTimeSlot_StartTimeAsc(branchId, courtType);

        model.addAttribute("court", court);
        model.addAttribute("branchId", branchId);
        model.addAttribute("prices", prices);

        return "admin/court/edit";
    }

    @Transactional
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) List<Long> priceIds,
                         @RequestParam(required = false) List<BigDecimal> prices,
                         @ModelAttribute Court req,
                         RedirectAttributes redirectAttributes) {

        Court c = courtRepo.findById(id).orElseThrow();
        Long branchId = c.getBranch().getId();
        String oldCourtType = normalizeCourtType(c.getType());
        String newCourtType = normalizeCourtType(req.getType());
        List<Price> currentPrices = priceRepo.findByBranchIdAndCourtTypeIgnoreCaseOrderByTimeSlot_StartTimeAsc(branchId, oldCourtType);
        List<Long> currentPriceIds = currentPrices.stream()
                .map(Price::getId)
                .toList();

        if (!sameCourtType(oldCourtType, newCourtType)
                && !currentPriceIds.isEmpty()
                && priceRepo.existsByBranch_IdAndCourtTypeIgnoreCaseAndIdNotIn(branchId, newCourtType, currentPriceIds)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Loại sân này đã có bảng giá sẵn ở chi nhánh. Hãy dùng loại khác hoặc cập nhật đúng sân đang dùng bảng giá đó.");
            return "redirect:/admin/courts/edit/" + id;
        }

        c.setName(req.getName());
        c.setType(req.getType());
        c.setStatus(req.getStatus());
        courtRepo.save(c);

        if (priceIds != null && prices != null) {
            int size = Math.min(priceIds.size(), prices.size());

            for (int i = 0; i < size; i++) {
                Long priceId = priceIds.get(i);
                BigDecimal amount = prices.get(i);

                if (priceId == null || amount == null) {
                    continue;
                }

                Price existingPrice = priceRepo.findByIdAndBranch_Id(priceId, branchId).orElseThrow();
                existingPrice.setCourtType(newCourtType);
                existingPrice.setPrice(amount);
                priceRepo.save(existingPrice);
            }
        } else {
            currentPrices.forEach(price -> {
                price.setCourtType(newCourtType);
                priceRepo.save(price);
            });
        }

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

        var prices = priceRepo.findByBranchId(branchId).stream()
                .sorted(Comparator.comparing(price -> price.getTimeSlot().getStartTime()))
                .toList();

        model.addAttribute("prices", prices);
        model.addAttribute("branchId", branchId);

        return "admin/price/edit";
    }

    @Transactional
    @PostMapping("/prices/update")
    public String updatePrices(@RequestParam(required = false) List<Long> priceIds,
                               @RequestParam(required = false) List<BigDecimal> prices) {

        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = userRepo.findByEmail(((UserDetails) principal).getUsername())
                .orElseThrow();

        Long branchId = user.getManagedBranch().getId();

        if (priceIds != null && prices != null) {
            int size = Math.min(priceIds.size(), prices.size());

            for (int i = 0; i < size; i++) {
                Long priceId = priceIds.get(i);
                BigDecimal amount = prices.get(i);

                if (priceId == null || amount == null) {
                    continue;
                }

                Price existingPrice = priceRepo.findByIdAndBranch_Id(priceId, branchId).orElseThrow();
                existingPrice.setPrice(amount);
                priceRepo.save(existingPrice);
            }
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

    private String normalizeCourtType(String courtType) {
        return courtType == null ? null : courtType.trim();
    }

    private boolean sameCourtType(String left, String right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }
}
