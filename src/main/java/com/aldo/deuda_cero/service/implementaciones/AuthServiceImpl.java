package com.aldo.deuda_cero.service.implementaciones;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
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
import com.aldo.deuda_cero.mapper.RegistroMapper;
import com.aldo.deuda_cero.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final CurrentUserService currentUserService;

    @Value("${jwt.expirationRefreshToken}")
    private long jwtExpirationRefreshToken;

    @Override
    public RegistroResponse registro(RegistroRequest registroRequest) {

        if(usuarioRepository.findByEmail(registroRequest.getEmail()).isPresent()){
            throw new EmailExistenteException();
        }

        Usuario usuario = RegistroMapper.toEntity(registroRequest);

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return RegistroMapper.toResponse(usuarioGuardado);
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

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new UsuarioNoEncontradoException());
        
        refreshTokenRepository.deleteByUsuario(usuario);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
            .token(refreshToken)
            .usuario(usuario)
            .expirationDate(LocalDateTime.now().plusSeconds(jwtExpirationRefreshToken / 1000))
            .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return LoginMapper.toResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional(noRollbackFor = {DisabledException.class, RefreshTokenExpiradoException.class})
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshTokenRequest.getRefreshToken())
            .orElseThrow(() -> new RefreshTokenNoEncontradoException());

        if(refreshTokenEntity.getExpirationDate().isBefore(LocalDateTime.now())){
            refreshTokenRepository.deleteByToken(refreshTokenRequest.getRefreshToken());
            throw new RefreshTokenExpiradoException();
        }

        Usuario usuario = refreshTokenEntity.getUsuario();
        String email = usuario.getEmail();

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if(!userDetails.isEnabled()){
            refreshTokenRepository.deleteByUsuario(usuario);
            throw new DisabledException("La cuenta está desactivada");
        }

        if(!jwtService.isValid(refreshTokenRequest.getRefreshToken(), userDetails)){
            throw new RefreshTokenNoValidoException();
        }

        String nuevoAccesToken = jwtService.generateAccessToken(userDetails);

        return LoginMapper.toResponse(nuevoAccesToken, refreshTokenRequest.getRefreshToken());
    }

    @Override
    @Transactional
    public String logout(LogoutRequest logoutRequest) {
        
        if(!refreshTokenRepository.findByToken(logoutRequest.getRefreshToken()).isPresent()){
            throw new RefreshTokenNoEncontradoException();
        }

        refreshTokenRepository.deleteByToken(logoutRequest.getRefreshToken());

        return "Refresh token eliminado / sesion cerrada";

    }

    @Override
    public MeResponse me() {

        Usuario usuario = currentUserService.obtenerUsuarioActual();

        return new MeResponse(usuario.getNombre(), usuario.getEmail());
    }
    
}
