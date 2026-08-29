package com.aldo.deuda_cero.pagoTest;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.deuda_cero.controller.PagoDeudaController;
import com.aldo.deuda_cero.dto.pagos.RegistrarPagoRequest;
import com.aldo.deuda_cero.dto.pagos.PagoResponse;
import com.aldo.deuda_cero.security.CustomUserDetailsService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.interfaces.GrupoService;
import com.aldo.deuda_cero.service.interfaces.PagoDeudaService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PagoDeudaController.class)
public class PagoDeudaControllerTest {
    
    @MockitoBean
    private PagoDeudaService pagoDeudaService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private GrupoService grupoService;

    @Test
    @WithMockUser
    void deberiaRegistrarPagoCorrectamente() throws Exception {

        Long grupoId = 1L;

        RegistrarPagoRequest request = new RegistrarPagoRequest();
        request.setDeudaId(1L);
        request.setMonto(new BigDecimal(500));

        mockMvc.perform(
            post("/pagos/{grupoId}/registrar", grupoId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deberiaObtenerPagos() throws Exception {

        Long grupoId = 1L;

        List<PagoResponse> response = List.of(
            new PagoResponse(1L, "Aldo", "Odla", new BigDecimal(500), LocalDateTime.now())
        );

        when(pagoDeudaService.obtenerPagos(grupoId)).thenReturn(response);

        mockMvc.perform(
            get("/pagos/{grupoId}/pagos", grupoId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].pagador").value("Aldo"))
        .andExpect(jsonPath("$[0].receptor").value("Odla"));

    }
}
