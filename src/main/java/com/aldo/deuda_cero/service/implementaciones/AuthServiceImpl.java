package com.aldo.deuda_cero.service.implementaciones;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.deuda_cero.dto.auth.LoginRequest;
import com.aldo.deuda_cero.dto.auth.LoginResponse;
import com.aldo.deuda_cero.dto.auth.LogoutRequest;
import com.aldo.deuda_cero.dto.auth.MeResponse;
import com.aldo.deuda_cero.dto.auth.RefreshTokenRequest;
import com.aldo.deuda_cero.dto.auth.RegistroRequest;
import com.aldo.deuda_cero.dto.auth.RegistroResponse;
import com.aldo.deuda_cero.entity.RefreshToken;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.exception.EmailExistenteException;
import com.aldo.deuda_cero.exception.RefreshTokenExpiradoException;
import com.aldo.deuda_cero.exception.RefreshTokenNoEncontradoException;
import com.aldo.deuda_cero.exception.RefreshTokenNoValidoException;
import com.aldo.deuda_cero.exception.UsuarioNoEncontradoException;
import com.aldo.deuda_cero.mapper.LoginMapper;
import com.aldo.deuda_cero.mapper.ResgistroMapper;
import com.aldo.deuda_cero.repository.RefreshTokenResporitory;
import com.aldo.deuda_cero.repository.UsuarioRepository;
import com.aldo.deuda_cero.security.CurrentUserService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.interfaces.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenResporitory refreshTokenResporitory;
    private final CurrentUserService currentUserService;

    @Override
    public RegistroResponse registro(RegistroRequest registroRequest) {

        if(usuarioRepository.findByEmail(registroRequest.getEmail()).isPresent()){
            throw new EmailExistenteException();
        }

        Usuario usuario = ResgistroMapper.toEntity(registroRequest);

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return ResgistroMapper.toResponse(usuarioGuardado);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
            .token(refreshToken)
            .usuario(usuarioRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new UsuarioNoEncontradoException()))
            .expirationDate(LocalDateTime.now().plusDays(1))
            .build();

        refreshTokenResporitory.save(refreshTokenEntity);

        return LoginMapper.toResponse(accessToken, refreshToken);
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        
        RefreshToken refreshTokenEntity = refreshTokenResporitory.findByToken(refreshTokenRequest.getRefreshToken())
            .orElseThrow(() -> new RefreshTokenNoEncontradoException());

        if(refreshTokenEntity.getExpirationDate().isBefore(LocalDateTime.now())){
            refreshTokenResporitory.deleteByToken(refreshTokenRequest.getRefreshToken());
            throw new RefreshTokenExpiradoException();
        }

        Usuario usuario = refreshTokenEntity.getUsuario();
        String email = usuario.getEmail();

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if(!jwtService.isValid(refreshTokenRequest.getRefreshToken(), userDetails)){
            throw new RefreshTokenNoValidoException();
        }

        String nuevoAccesToken = jwtService.generateAccessToken(userDetails);

        return LoginMapper.toResponse(nuevoAccesToken, refreshTokenRequest.getRefreshToken());
    }

    @Override
    @Transactional
    public String logout(LogoutRequest logoutRequest) {
        
        if(!refreshTokenResporitory.findByToken(logoutRequest.getRefreshToken()).isPresent()){
            throw new RefreshTokenNoEncontradoException();
        }

        refreshTokenResporitory.deleteByToken(logoutRequest.getRefreshToken());

        return "Refresh token eliminado / sesion cerrada";

    }

    @Override
    public MeResponse me() {
        
        //Aqui quitar las dos primeras lineas y cambiarlo por la clase CurrentUserService del paquete security
        
        /*
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("No se encontro el usuario"));
        */

        Usuario usuario = currentUserService.obtenerUsuarioActual();

        return new MeResponse(usuario.getNombre(), usuario.getEmail());
    }
    
}
