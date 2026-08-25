package com.aldo.deuda_cero.authTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.deuda_cero.controller.AuthController;
import com.aldo.deuda_cero.dto.auth.LoginRequest;
import com.aldo.deuda_cero.dto.auth.LoginResponse;
import com.aldo.deuda_cero.dto.auth.LogoutRequest;
import com.aldo.deuda_cero.dto.auth.MeResponse;
import com.aldo.deuda_cero.dto.auth.RefreshTokenRequest;
import com.aldo.deuda_cero.dto.auth.RegistroRequest;
import com.aldo.deuda_cero.dto.auth.RegistroResponse;
import com.aldo.deuda_cero.security.CustomUserDetailsService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.interfaces.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    
    @MockitoBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    @Test
    @WithMockUser
    void deberiaRegistrarCorrectamente() throws Exception {

        RegistroRequest request = new RegistroRequest();
        request.setNombre("Aldo");
        request.setApellido("Ruiz");
        request.setEmail("aldo@test.com");
        request.setPassword("test@321");

        RegistroResponse response = new RegistroResponse("Aldo", "Ruiz", "aldo@test.com", "USER");

        when(authService.registro(any(RegistroRequest.class))).thenReturn(response);

        mockMvc.perform(
            post("/auth/registro")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nombre").value("Aldo"))
        .andExpect(jsonPath("$.email").value("aldo@test.com"));
    }

    @Test
    @WithMockUser
    void deberiaLoguearCorrectamente() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("aldo@test.com");
        request.setPassword("test@321");

        LoginResponse respnonse = new LoginResponse("access-token", "refresh-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(respnonse);

        mockMvc.perform(
            post("/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @WithMockUser
    void deberiaHacerLogoutCorrectamente() throws Exception {

        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        when(authService.logout(any(LogoutRequest.class))).thenReturn("Sesion cerrada");

        mockMvc.perform(
                    post("/auth/logout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser
    void deberiaRefrescarElRefreshTokenCorrectamente() throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        LoginResponse response = new LoginResponse("nuevo-access-token", "refresh-token");

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(
            post("/auth/refresh-token")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("nuevo-access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @WithMockUser
    void deberiaMostrarMiInformacionCorrectamente() throws Exception {

        MeResponse respoonse = new MeResponse("Aldo", "aldo@test321");

        mockMvc.perform(
            get("/auth/me")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(respoonse))
        )
        .andExpect(status().isOk());
    }
}
