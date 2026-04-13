package com.badminton.booking.notification.service.impl;

import com.badminton.booking.domain.entity.Notification;
import com.badminton.booking.domain.entity.User;
import com.badminton.booking.notification.dto.response.NotificationResponse;
import com.badminton.booking.notification.repository.NotificationRepository;
import com.badminton.booking.domain.repository.UserRepository;
import com.badminton.booking.notification.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {
    NotificationRepository notificationRepository;
    UserRepository userRepository;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getTop10Notifications(Long userId){
        List<Notification> rawList = notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        List<NotificationResponse> list = new ArrayList<>();

        for(Notification data : rawList){
            Long id = data.getId();
            String title = data.getTitle();
            String content = data.getContent();
            Boolean isRead = data.getIsRead();
            String timeDisplay = formatter.format(data.getCreatedAt());
            list.add(new NotificationResponse(id, title, content, isRead, timeDisplay));
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotificationsPaging(Long userId, int pageNo, int pageSize){
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());

        Page<Notification> notiPage = notificationRepository.findByUserId(userId, pageable);

        return notiPage.map(noti -> {
            NotificationResponse dto = new NotificationResponse();
            dto.setId(noti.getId());
            dto.setTitle(noti.getTitle());
            dto.setContent(noti.getContent());
            dto.setIsRead(noti.getIsRead());
            dto.setTimeDisplay(formatter.format(noti.getCreatedAt()));
            return dto;
        });
    }

    @Override
    @Transactional
    public void createNotification(Long userId, String title, String content, String actionUrl){
        User user = userRepository.findById(userId).orElse(null);
        Notification notification = new Notification();

        if(user != null){
            notification.setUser(user);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setActionUrl(actionUrl);
        }
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public String viewDetailNotification(Long id){
        Notification noti = notificationRepository.findById(id).orElse(null);
        if(noti != null){
            if(!noti.getIsRead()){
                noti.setIsRead(true);
                notificationRepository.save(noti);
            }
            return noti.getActionUrl();
        }
        return null;
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId){
        notificationRepository.markAllAsRead(userId);
    }
}
