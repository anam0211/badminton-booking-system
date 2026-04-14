package com.badminton.booking.notification.controller;

import com.badminton.booking.domain.entity.User;
import com.badminton.booking.notification.dto.response.NotificationResponse;
import com.badminton.booking.notification.service.NotificationService;
import com.badminton.booking.security.CustomUserDetails;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;

    @GetMapping("/notifications")
    public String viewAllNotifications(@RequestParam(defaultValue = "0") int page, Model model,
                                       @AuthenticationPrincipal CustomUserDetails customUserDetails){

        int pageSize = 10;
        Page<NotificationResponse> notificationPage;
        if(customUserDetails != null){
            User user = customUserDetails.getUser();
            notificationPage = notificationService.getAllNotificationsPaging(user.getId(), page, pageSize);
        }
        else{
            notificationPage = Page.empty();
        }
        model.addAttribute("notificationPage", notificationPage);

        return "notification/list";
    }

    @GetMapping("/notifications/read/{id}")
    public String viewDetailNotification(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String referer){
        String actionUrl = notificationService.viewDetailNotification(id);
        if(actionUrl == null){
            if(referer != null && !referer.isEmpty()){
                return "redirect:" + referer;
            }
            else return "redirect:/notifications";
        }
        return "redirect:" + actionUrl;
    }

    @PostMapping("/notifications/mark-all-read")
    public ResponseEntity<String> markAllAsRead(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
        }
        User user = customUserDetails.getUser();
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok("success");
    }
}
