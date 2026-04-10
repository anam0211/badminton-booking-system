package com.badminton.booking.booking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class BookingListPageData {
    private Long defaultBranchId;
    private boolean adminView;
    private String pageTitle;
    private String pageDescription;
    private String emptyMessage;
    private Integer totalBookings;
    private BigDecimal totalAmount;
    private Integer activeBookings;
    private Integer cancelledBookings;
    private List<BookingResponse> bookings;
}
