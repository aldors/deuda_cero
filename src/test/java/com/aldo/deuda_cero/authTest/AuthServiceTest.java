package com.aldo.deuda_cero.authTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aldo.deuda_cero.dto.auth.LoginRequest;
import com.aldo.deuda_cero.dto.auth.LoginResponse;
import com.aldo.deuda_cero.dto.auth.LogoutRequest;
import com.aldo.deuda_cero.dto.auth.MeResponse;
import com.aldo.deuda_cero.dto.auth.RefreshTokenRequest;
import com.aldo.deuda_cero.dto.auth.RegistroRequest;
import com.aldo.deuda_cero.dto.auth.RegistroResponse;
import com.aldo.deuda_cero.entity.RefreshToken;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.Role;
import com.aldo.deuda_cero.exception.EmailExistenteException;
import com.aldo.deuda_cero.exception.RefreshTokenExpiradoException;
import com.aldo.deuda_cero.exception.RefreshTokenNoEncontradoException;
import com.aldo.deuda_cero.exception.RefreshTokenNoValidoException;
import com.aldo.deuda_cero.exception.UsuarioNoEncontradoException;
import com.aldo.deuda_cero.repository.RefreshTokenResporitory;
import com.aldo.deuda_cero.repository.UsuarioRepository;
import com.aldo.deuda_cero.security.CurrentUserService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.implementaciones.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenResporitory refreshTokenResporitory;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private RegistroRequest registroRequest;
    private Usuario usuario;
    private LoginRequest loginRequest;
    private LogoutRequest logoutRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp(){

        usuario = Usuario.builder()
            .nombre("Aldo")
            .apellido("Ruiz")
            .email("aldo@test.com")
            .password("test@321")
            .activo(true)
            .fechaCreacion(LocalDateTime.now())
            .fechaActualizacion(LocalDateTime.now())
            .role(Role.USER)
            .build();

        registroRequest = new RegistroRequest();

        registroRequest.setNombre("Aldo");
        registroRequest.setApellido("Ruiz");
        registroRequest.setEmail("aldo@test.com");
        registroRequest.setPassword("test@321");

        loginRequest = new LoginRequest();

        loginRequest.setEmail("aldo@test.com");
        loginRequest.setPassword("test@321");

        logoutRequest = new LogoutRequest();

        logoutRequest.setRefreshToken("refresh-token");

        refreshTokenRequest = new RefreshTokenRequest();

        refreshTokenRequest.setRefreshToken("refresh-token");


        //Simulacion de un usuario autenticado
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }


    /* HAPPY PATH */
    @Test
    void deberiaRegistrarCorrectamente(){

        when(usuarioRepository.findByEmail(registroRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registroRequest.getPassword())).thenReturn("contraseña encriptada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        RegistroResponse response = authServiceImpl.registro(registroRequest);

        assertNotNull(response);
        assertEquals(registroRequest.getNombre(), response.getNombre());
        assertEquals("USER", response.getRol());

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deberiaLoguearCorrectamente(){

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(loginRequest.getEmail())
                .password("contraseña encriptada")
                .roles("USER")
                .build();

        when(userDetailsService.loadUserByUsername(loginRequest.getEmail())).thenReturn(userDetails);

        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        when(usuarioRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(usuario));

        when(refreshTokenResporitory.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

        LoginResponse response = authServiceImpl.login(loginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(refreshTokenResporitory).save(any(RefreshToken.class));
    }

    @Test
    void deberiaHacerLogoutCorrectamente(){

        when(refreshTokenResporitory.findByToken(logoutRequest.getRefreshToken())).thenReturn(Optional.of(new RefreshToken()));

        authServiceImpl.logout(logoutRequest);

        verify(refreshTokenResporitory).deleteByToken(logoutRequest.getRefreshToken());
        
    }

    @Test
    void deberiaRefrescarTokenCorrectamente(){

        RefreshToken tokenEntity = RefreshToken.builder()
                .token("refresh-token")
                .usuario(usuario)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenResporitory.findByToken(refreshTokenRequest.getRefreshToken())).thenReturn(Optional.of(tokenEntity));
        
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(loginRequest.getEmail())
                .password("contraseña encriptada")
                .roles("USER")
                .build();
            
        when(userDetailsService.loadUserByUsername(usuario.getEmail())).thenReturn(userDetails);

        when(jwtService.isValid(refreshTokenRequest.getRefreshToken(), userDetails)).thenReturn(true);

        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token-nuevo");

        LoginResponse response = authServiceImpl.refreshToken(refreshTokenRequest);

        assertNotNull(response);
        assertEquals("access-token-nuevo", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(refreshTokenResporitory).findByToken(refreshTokenRequest.getRefreshToken());
    }

    @Test
    void deberiaMostrarmeMiInformacion(){

        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);

        MeResponse response = authServiceImpl.me();

        assertNotNull(response);
        assertEquals("Aldo", response.getNombre());
        assertEquals("aldo@test.com", response.getEmail());
    }

    // No funciona
    @Test
    void deberiaRetornarUsuarioExistente() {

        when(usuarioRepository.findByEmail("aldo@test.com")).thenReturn(Optional.of(usuario));

        Usuario user = currentUserService.obtenerUsuarioActual();

        assertNotNull(user);
    }


    /* ERRROR PATH */
    @Test
    void deberiaLanzarExcepcionSiElEmailYaEstaRegistrado(){

        when(usuarioRepository.findByEmail(registroRequest.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(EmailExistenteException.class, () -> authServiceImpl.registro(registroRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioNoEncontrado(){

        when(usuarioRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> authServiceImpl.login(loginRequest));
    }

    // RefreshToken no encontrado para el metodo refreshToken()
    @Test 
    void deberiaLanzarExcepcionSiRefreshTokenNoEncontrado(){

        when(refreshTokenResporitory.findByToken("refresh-token")).thenReturn(Optional.empty());

        assertThrows(RefreshTokenNoEncontradoException.class, () -> authServiceImpl.refreshToken(refreshTokenRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiRefreshTokenExpirado(){

        RefreshToken tokenEntity = RefreshToken.builder()
                .token("refresh-token")
                .usuario(usuario)
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build();
        
        when(refreshTokenResporitory.findByToken("refresh-token")).thenReturn(Optional.of(tokenEntity));

        assertThrows(RefreshTokenExpiradoException.class, () -> authServiceImpl.refreshToken(refreshTokenRequest));

    }

    @Test
    void deberiaLanzarExcepcionSiRefreshTokenNoValido(){

        RefreshToken tokenEntity = RefreshToken.builder()
                .token("refresh-token")
                .usuario(usuario)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenResporitory.findByToken("refresh-token")).thenReturn(Optional.of(tokenEntity));
        
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(loginRequest.getEmail())
                .password("contraseña encriptada")
                .roles("USER")
                .build();
            
        when(userDetailsService.loadUserByUsername(usuario.getEmail())).thenReturn(userDetails);

        when(jwtService.isValid("refresh-token", userDetails)).thenReturn(false);

        assertThrows(RefreshTokenNoValidoException.class, () -> authServiceImpl.refreshToken(refreshTokenRequest));

    }

    // RefreshToken no encontrado para el metodo logout()
    @Test 
    void deberiaLanzarExcepcionSiRefreshTokenNoEncontradoLogout(){

        when(refreshTokenResporitory.findByToken("refresh-token")).thenReturn(Optional.empty());

        assertThrows(RefreshTokenNoEncontradoException.class, () -> authServiceImpl.logout(logoutRequest));
    }

    // No funciona
    @Test
    void deberiaLanzarExcepcionSiUsuarioNoExiste() {

        when(usuarioRepository.findByEmail("no-existe@test.com")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> currentUserService.obtenerUsuarioActual());
    }

}
