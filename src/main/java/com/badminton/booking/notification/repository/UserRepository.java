package com.badminton.booking.notification.repository;

import com.badminton.booking.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserById(Long userId);
    Optional<User> findByEmailIgnoreCase(String username);
}
