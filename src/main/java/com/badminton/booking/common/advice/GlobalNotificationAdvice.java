package com.badminton.booking.common.advice;

import com.badminton.booking.domain.entity.User;
import com.badminton.booking.notification.dto.response.NotificationResponse;
import com.badminton.booking.notification.service.NotificationService;
import com.badminton.booking.security.CustomUserDetails;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GlobalNotificationAdvice {
    NotificationService notificationService;

    @ModelAttribute
    public void addGlobalAttribute(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        if(customUserDetails != null){
            User user = customUserDetails.getUser();
            List<NotificationResponse> list = notificationService.getTop10Notifications(user.getId());

            // Kiểm tra thông báo chưa đọc để phía view hiển thị chấm đỏ
            boolean hasUnread = list.stream().anyMatch(n -> !n.getIsRead());

            model.addAttribute("notifications", list);
            model.addAttribute("hasUnread", hasUnread);
        }
        else{
            model.addAttribute("notifications", Collections.emptyList());
            model.addAttribute("hasUnread", false);
            model.addAttribute("currentUser", null);
        }
    }
}
