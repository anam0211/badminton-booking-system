package com.badminton.booking.common.advice;

import com.badminton.booking.domain.entity.User;
import com.badminton.booking.notification.dto.response.NotificationResponse;
import com.badminton.booking.notification.service.NotificationService;
import com.badminton.booking.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
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
    public void addGlobalAttribute(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                   HttpServletRequest request){
        String currentUrl = request.getRequestURI();

        if(customUserDetails != null){
            User currentUser = customUserDetails.getUser();
            model.addAttribute("currentUser", currentUser);

            if(!currentUrl.startsWith("/admin")){
                List<NotificationResponse> list = notificationService.getTop10Notifications(currentUser.getId());

                // Kiểm tra thông báo chưa đọc để phía view hiển thị chấm đỏ
                boolean hasUnread = list.stream().anyMatch(n -> !n.getIsRead());

                model.addAttribute("notifications", list);
                model.addAttribute("hasUnread", hasUnread);
            }
        }
        else{
            if(!currentUrl.startsWith("/admin")){
                model.addAttribute("notifications", Collections.emptyList());
                model.addAttribute("hasUnread", false);
            }
            model.addAttribute("currentUser", null);
        }
    }
}
