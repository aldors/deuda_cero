package com.aldo.deuda_cero.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    
    private String refreshToken;
}
