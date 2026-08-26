package com.aldo.deuda_cero.invitacionTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.deuda_cero.controller.InvitacionController;
import com.aldo.deuda_cero.dto.invitaciones.AceptarInvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionesPendientesResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitarMiembrosRequest;
import com.aldo.deuda_cero.entity.enums.EstadoInvitacion;
import com.aldo.deuda_cero.security.CustomUserDetailsService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.interfaces.InvitacionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(InvitacionController.class)
public class InvitacionControllerTest {
    
    @MockitoBean
    private InvitacionService invitacionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void deberiaInvitarUsuarioCorrectamente() throws Exception {

        Long grupoId = 1L;

        InvitarMiembrosRequest request = new InvitarMiembrosRequest("aldo@test.com");

        InvitacionResponse response = new InvitacionResponse(1L, "Grupo invitación", "Aldo", EstadoInvitacion.PENDIENTE);

        when(invitacionService.invitarUsuarios(any(), any(InvitarMiembrosRequest.class))).thenReturn(response);

        mockMvc.perform(
            post("/invitaciones/invitar/{grupoId}", grupoId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.invitado").value("Aldo"));
    }

    @Test
    @WithMockUser
    void deberiaObtenerInvitacionesCorrectamente() throws Exception {

        List<InvitacionesPendientesResponse> response = List.of(
            new InvitacionesPendientesResponse(1L, "Grupo invitación", "Aldo", EstadoInvitacion.PENDIENTE, LocalDateTime.now())
        );

        when(invitacionService.obtenerInvitaciones()).thenReturn(response);

        mockMvc.perform(
            get("/invitaciones/invitaciones")
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser
    void deberiaAceptarInvitacionCorrectamente() throws Exception {

        Long invitacionId = 1L;

        AceptarInvitacionResponse response = new AceptarInvitacionResponse(1L, "grupo invitación", "Bienvenido al grupo");

        when(invitacionService.aceptarInvitacion(invitacionId)).thenReturn(response);

        mockMvc.perform(
            post("/invitaciones/invitaciones/{invitacionId}/aceptar", invitacionId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.grupoId").value(1L))
        .andExpect(jsonPath("$.mensaje").value("Bienvenido al grupo"));
    }

    @Test
    @WithMockUser
    void deberiaRechazarInvitacionCorrectamente() throws Exception {

        Long invitacionId = 1L;

        AceptarInvitacionResponse response = new AceptarInvitacionResponse(1L, "grupo invitación", "Invitación rechazada correctamente");

        when(invitacionService.rechazarInvitacion(invitacionId)).thenReturn(response);

        mockMvc.perform(
            post("/invitaciones/invitaciones/{invitacionId}/rechazar", invitacionId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.grupoId").value(1L))
        .andExpect(jsonPath("$.mensaje").value("Invitación rechazada correctamente"));
    }

}
