package com.badminton.booking.notification.service;

import com.badminton.booking.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getTop10Notifications(Long userId);

    void createNotification(Long userId, String title, String content, String actionUrl);

    Page<NotificationResponse> getAllNotificationsPaging(Long userId, int pageNo, int pageSize);

    String viewDetailNotification(Long id);

    void markAllAsRead(Long userId);
}
