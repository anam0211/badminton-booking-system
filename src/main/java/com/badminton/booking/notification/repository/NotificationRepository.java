package com.badminton.booking.notification.repository;

import com.badminton.booking.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Optional<Notification> findById(Long id);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Notification n
        SET n.isRead = true
        WHERE n.user.id = :userId
        AND n.isRead = false
    """)
    void markAllAsRead(@RequestParam("userId") Long userId);
}
