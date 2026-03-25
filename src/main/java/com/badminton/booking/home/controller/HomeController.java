package com.badminton.booking.home.controller;

import com.badminton.booking.home.dto.response.HomeResponse;
import com.badminton.booking.home.service.HomeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomeController {
    HomeService homeService;

    @GetMapping("/home")
    public String getBranchList(Model model){
        List<HomeResponse> list = homeService.getBranchList();
        model.addAttribute("branchList", list);
        return "home/home";
    }
}
