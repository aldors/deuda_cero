package com.aldo.deuda_cero.balanceTest;

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

import com.aldo.deuda_cero.controller.BalanceController;
import com.aldo.deuda_cero.dto.balance.BalanceMiembroResponse;
import com.aldo.deuda_cero.security.CustomUserDetailsService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.interfaces.BalanceService;

@WebMvcTest(BalanceController.class)
public class BalanceControllerTest {

    @MockitoBean
    private BalanceService balanceService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void deberiaObtenerBalanceCorrectamente() throws Exception {

        Long grupoId = 1L;

        List<BalanceMiembroResponse> response = List.of(
            new BalanceMiembroResponse(1L, "Aldo", new BigDecimal(300), new BigDecimal(200), new BigDecimal(100))
        );

        when(balanceService.obtenerBalance(grupoId)).thenReturn(response);

        mockMvc.perform(
            get("/balances/{grupoId}/balance", grupoId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].miembroGrupoId").value(1L))
        .andExpect(jsonPath("$[0].balance").value(new BigDecimal(100)));
        
    }
    
}
