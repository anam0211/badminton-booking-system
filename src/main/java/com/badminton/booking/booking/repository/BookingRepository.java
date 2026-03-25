package com.badminton.booking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.badminton.booking.domain.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
}