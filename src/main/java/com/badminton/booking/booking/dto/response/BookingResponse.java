package com.badminton.booking.booking.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String branchName;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private String note;
    private LocalDateTime bookingDate;
    private LocalDateTime updatedAt;
    private Integer totalSlots;
    private LocalDate earliestPlayDate;
    private LocalDate latestPlayDate;
    private List<BookingDetailResponse> items;
}
