package com.badminton.booking.booking.repository;

import com.badminton.booking.domain.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select bd
            from BookingDetail bd
            join fetch bd.booking b
            where bd.court.branch.id = :branchId
              and bd.playDate = :playDate
              and bd.activeKey = :activeKey
            """)
    List<BookingDetail> findActiveDetailsWithBookingByBranchAndPlayDate(@Param("branchId") Long branchId,
                                                                        @Param("playDate") LocalDate playDate,
                                                                        @Param("activeKey") byte activeKey);

    List<BookingDetail> findByBooking_Id(Long bookingId);

    @Query("""
            select bd
            from BookingDetail bd
            join fetch bd.court c
            join fetch c.branch
            join fetch bd.timeSlot ts
            where bd.booking.id = :bookingId
            order by bd.playDate asc, ts.startTime asc, bd.id asc
            """)
    List<BookingDetail> findDetailedByBookingId(@Param("bookingId") Long bookingId);

    @Query("""
            select bd
            from BookingDetail bd
            join fetch bd.booking b
            join fetch bd.court c
            join fetch c.branch
            join fetch bd.timeSlot ts
            where b.id in :bookingIds
            order by bd.playDate asc, ts.startTime asc, bd.id asc
            """)
    List<BookingDetail> findDetailedByBookingIds(@Param("bookingIds") List<Long> bookingIds);
}
