package com.aldo.deuda_cero.service.interfaces;

import com.aldo.deuda_cero.dto.auth.LoginRequest;
import com.aldo.deuda_cero.dto.auth.LoginResponse;
import com.aldo.deuda_cero.dto.auth.LogoutRequest;
import com.aldo.deuda_cero.dto.auth.MeResponse;
import com.aldo.deuda_cero.dto.auth.RefreshTokenRequest;
import com.aldo.deuda_cero.dto.auth.RegistroRequest;
import com.aldo.deuda_cero.dto.auth.RegistroResponse;

public interface AuthService {
    
    public RegistroResponse registro(RegistroRequest registroRequest);
    public LoginResponse login(LoginRequest loginRequest);
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    public String logout(LogoutRequest logoutRequest);
    public MeResponse me();
}
