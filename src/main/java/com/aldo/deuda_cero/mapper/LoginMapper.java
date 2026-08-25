package com.aldo.deuda_cero.mapper;

import org.springframework.stereotype.Component;

import com.aldo.deuda_cero.dto.auth.LoginResponse;

@Component
public class LoginMapper {
    
    public static LoginResponse toResponse(String accessToken, String refreshToken){
        return new LoginResponse(accessToken, refreshToken);
    }
}
