package com.badminton.booking.dashboard.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BranchMonthlyDashboardRequest {
    Long branchId;
    int month;
    int year;
}
