package com.aldo.deuda_cero.deudaTest;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.deuda_cero.controller.DeudaController;
import com.aldo.deuda_cero.dto.deuda.DeudaResponse;
import com.aldo.deuda_cero.entity.enums.EstadoDeuda;
import com.aldo.deuda_cero.security.CustomUserDetailsService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.interfaces.DeudaService;

@WebMvcTest(DeudaController.class)
public class DeudaControllerTest {
    
    @MockitoBean
    private DeudaService deudaService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void deberiaObtenerDeudasCorrectamente() throws Exception {

        Long grupoId = 1L;

        DeudaResponse deudaResponse = new DeudaResponse(1L, 1L, "Aldo", 2L, "Odla", new BigDecimal(500), new BigDecimal(250), EstadoDeuda.PENDIENTE);

        List<DeudaResponse> response = List.of(deudaResponse);

        when(deudaService.obtenerDeudas(grupoId)).thenReturn(response);

        mockMvc.perform(
            get("/grupos/{grupoId}/deudas", grupoId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].deudor").value("Aldo"))
        .andExpect(jsonPath("$[0].acreedor").value("Odla"));
    }

    @Test
    @WithMockUser
    void deberiaObtenerDeudasPendientesCorrectamente() throws Exception{

        Long grupoId = 1L;

        DeudaResponse deudaResponse = new DeudaResponse(1L, 1L, "Aldo", 2L, "Odla", new BigDecimal(500), new BigDecimal(250), EstadoDeuda.PENDIENTE);

        List<DeudaResponse> response = List.of(deudaResponse);

        when(deudaService.obtenerDeudasPendientes(grupoId)).thenReturn(response);

        mockMvc.perform(
            get("/grupos/{grupoId}/deudas/pendientes", grupoId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].deudorId").value(1L))
        .andExpect(jsonPath("$[0].acreedorId").value(2L));
    }

    @Test
    @WithMockUser
    void deberiaObtnerDeudasPorIdCorrectamente() throws Exception {

        Long grupoId = 1L;
        Long deudaId = 2L;

        DeudaResponse response = new DeudaResponse(1L, 1L, "Aldo", 2L, "Odla", new BigDecimal(500), new BigDecimal(250), EstadoDeuda.PENDIENTE);

        when(deudaService.obtenerDeudaPorId(grupoId, deudaId)).thenReturn(response);

        mockMvc.perform(
            get("/grupos/{grupoId}/deudas/{deudaId}", grupoId, deudaId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deudorId").value(1L))
        .andExpect(jsonPath("$.acreedorId").value(2L));
    }
}
