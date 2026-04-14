package com.badminton.booking.area.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaResponse {
    private Integer id;
    private String name;
    private Integer branchCount;
}
