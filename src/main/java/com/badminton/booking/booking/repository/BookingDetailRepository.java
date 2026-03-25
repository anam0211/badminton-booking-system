package com.badminton.booking.booking.repository;

import com.badminton.booking.domain.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {

    boolean existsByCourt_IdAndTimeSlot_IdAndPlayDateAndActiveKey(
            Long courtId,
            Integer timeSlotId,
            LocalDate playDate,
            byte activeKey
    );

    List<BookingDetail> findByCourt_Branch_IdAndPlayDateAndActiveKey(
            Long branchId,
            LocalDate playDate,
            byte activeKey
    );
      List<BookingDetail> findByBooking_Id(Long bookingId);
}