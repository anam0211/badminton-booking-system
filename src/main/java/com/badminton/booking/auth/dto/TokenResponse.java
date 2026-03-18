package com.badminton.booking.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String tokenType;
    private String accessToken;
}
