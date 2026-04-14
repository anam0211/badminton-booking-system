package com.badminton.booking.booking.service;

import com.badminton.booking.booking.repository.BookingCourtRepository;
import com.badminton.booking.booking.repository.BookingPriceRepository;
import com.badminton.booking.common.exception.AppException;
import com.badminton.booking.common.exception.ErrorCode;
import com.badminton.booking.domain.entity.Court;
import com.badminton.booking.domain.entity.Price;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingPricingService {

    private final BookingCourtRepository courtRepository;
    private final BookingPriceRepository priceRepository;

    public BigDecimal calculate(Long courtId, Integer timeSlotId) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new AppException(ErrorCode.COURT_NOT_FOUND));
        return calculate(court, timeSlotId);
    }

    public BigDecimal calculate(Court court, Integer timeSlotId) {
        if (court == null) {
            throw new AppException(ErrorCode.COURT_NOT_FOUND);
        }

        Long branchId = court.getBranch().getId();
        String courtType = normalizeCourtType(court.getType());

        Price price = priceRepository
                .findByBranch_IdAndTimeSlot_IdAndCourtTypeIgnoreCase(branchId, timeSlotId, courtType)
                .or(() -> priceRepository.findByBranch_IdAndTimeSlot_IdAndCourtTypeIsNull(branchId, timeSlotId))
                .orElseThrow(() -> {
                    log.warn("PRICE_NOT_FOUND courtId={}, branchId={}, timeSlotId={}, courtType={}",
                            court.getId(), branchId, timeSlotId, courtType);
                    return new AppException(ErrorCode.PRICE_NOT_FOUND);
                });

        return price.getPrice();
    }

    private String normalizeCourtType(String courtType) {
        return courtType == null ? null : courtType.trim();
    }
}
