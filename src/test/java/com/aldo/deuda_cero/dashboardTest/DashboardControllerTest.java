package com.aldo.deuda_cero.dashboardTest;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.deuda_cero.controller.DashboardController;
import com.aldo.deuda_cero.dto.dashboard.DashboardResponse;
import com.aldo.deuda_cero.dto.dashboard.DeudaResumenResponse;
import com.aldo.deuda_cero.dto.dashboard.MovimientoRecienteResponse;
import com.aldo.deuda_cero.security.CustomUserDetailsService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.interfaces.DashboardService;

@WebMvcTest(DashboardController.class)
public class DashboardControllerTest {
    
    @MockitoBean
    private DashboardService dashboardService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void deberiaObtenerDashboardCorrectamente() throws Exception {

        Long grupoId = 1L;

        DashboardResponse response = new DashboardResponse(
            new BigDecimal(300),
            List.of(new DeudaResumenResponse(1L, 1L, "Aldo", new BigDecimal(200))),
            List.of(new DeudaResumenResponse(2L, 2L, "Odla", new BigDecimal(500))),
            List.of(new MovimientoRecienteResponse(1L, "Movimiento test", "Aldo", new BigDecimal(500), LocalDateTime.now()))
        );

        when(dashboardService.obtenerDashboard(grupoId)).thenReturn(response);

        mockMvc.perform(
            get("/grupos/{grupoId}/dashboard", grupoId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balanceGeneral").value(new BigDecimal(300)))
        .andExpect(jsonPath("$.movimientosRecientes.size()").value(1));
    }

}
