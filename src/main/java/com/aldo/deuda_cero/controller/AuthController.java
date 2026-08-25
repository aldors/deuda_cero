package com.aldo.deuda_cero.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.deuda_cero.dto.auth.LoginRequest;
import com.aldo.deuda_cero.dto.auth.LoginResponse;
import com.aldo.deuda_cero.dto.auth.LogoutRequest;
import com.aldo.deuda_cero.dto.auth.MeResponse;
import com.aldo.deuda_cero.dto.auth.RefreshTokenRequest;
import com.aldo.deuda_cero.dto.auth.RegistroRequest;
import com.aldo.deuda_cero.dto.auth.RegistroResponse;
import com.aldo.deuda_cero.service.interfaces.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    
    @PostMapping("/registro")
    public ResponseEntity<RegistroResponse> registro(@Valid @RequestBody RegistroRequest registroRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registro(registroRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest LogoutRequest){
        return ResponseEntity.ok(authService.logout(LogoutRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(){
        return ResponseEntity.ok(authService.me());
    }

    
    //PRUEBAS DE ENDPOINTS PROTEGIDOS PARA USUARIOS Y ADMINS
    @GetMapping("/usuarios")
    public String usuarios(){
        return "ERES UN USUARIO AUTENTICADO";
    }
    
    @GetMapping("/admins")
    public String admins(){
        return "ERES UN USUARIO AUTENTICADO Y ADEMAS ERES ADMINISTRADOR";
    }
}
