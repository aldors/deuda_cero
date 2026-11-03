package com.aldo.deuda_cero.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {
    
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
