package com.badminton.booking.booking.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.badminton.booking.domain.entity.Booking;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select b
            from Booking b
            join fetch b.user u
            where u.id = :userId
            order by b.bookingDate desc, b.id desc
            """)
    List<Booking> findBookingHistoryByUserId(@Param("userId") Long userId);

    @Query("""
            select b
            from Booking b
            join fetch b.user
            order by b.bookingDate desc, b.id desc
            """)
    List<Booking> findAllWithUserOrderByBookingDateDesc();
}
