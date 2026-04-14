package com.badminton.booking.court.repository;

import com.badminton.booking.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourtUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

}