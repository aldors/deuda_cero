package com.aldo.deuda_cero.movimientoTest;

import static org.mockito.ArgumentMatchers.any;
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

import com.aldo.deuda_cero.controller.MovimientoController;
import com.aldo.deuda_cero.dto.Movimientos.CrearMovimientoRequest;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoDetalleResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResumenResponse;
import com.aldo.deuda_cero.dto.Movimientos.ParticipacionResponse;
import com.aldo.deuda_cero.dto.Movimientos.ParticipanteMovimientoRequest;
import com.aldo.deuda_cero.entity.enums.TipoDivision;
import com.aldo.deuda_cero.entity.enums.TipoMovimiento;
import com.aldo.deuda_cero.security.CustomUserDetailsService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.interfaces.MovimientoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MovimientoController.class)
public class MovimientoControllerTest {
    
    @MockitoBean
    private MovimientoService movimientoService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void deberiaCrearMovimientoCorrectamente() throws Exception{

        Long grupoId = 1L;

        ParticipanteMovimientoRequest participantes = new ParticipanteMovimientoRequest();
        participantes.setUsuarioId(1L);
        participantes.setMonto(new BigDecimal(500));

        CrearMovimientoRequest request = new CrearMovimientoRequest();
        request.setDescripcion("Movimiento test");
        request.setMontoTotal(new BigDecimal(500));
        request.setPagadorId(1L);
        request.setTipoMovimiento(TipoMovimiento.GASTO);
        request.setTipoDivision(TipoDivision.PERSONALIZADA);
        request.setParticipantes(List.of(participantes));

        MovimientoResponse response = new MovimientoResponse(1L, request.getDescripcion(), request.getMontoTotal(), "Aldo", LocalDateTime.now());

        when(movimientoService.crearMovimiento(any(), any(CrearMovimientoRequest.class))).thenReturn(response);

        mockMvc.perform(
            post("/movimientos/crear/{grupoId}", grupoId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.descripcion").value("Movimiento test"));
    }

    @Test
    @WithMockUser
    void deberiaObtenerMovimientosCorrectamente() throws Exception{

        Long grupoId = 1L;

        List<MovimientoResumenResponse> response = List.of(
            new MovimientoResumenResponse(1L, "Movimiento resumen test", new BigDecimal(500),
                "Aldo", "Aldo", TipoMovimiento.GASTO, TipoDivision.PERSONALIZADA, LocalDateTime.now())
        );

        when(movimientoService.obtenerMovimientos(grupoId)).thenReturn(response);

        mockMvc.perform(
            get("/movimientos/{grupoId}/movimientos", grupoId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].tipo").value("GASTO"));
    }
    
    @Test
    @WithMockUser
    void deberiaObtenerDetallesCorrectamente() throws Exception{

        Long grupoId = 1L;
        Long movimientoId = 1L;

        MovimientoDetalleResponse response = new MovimientoDetalleResponse(1L, "Detalle test", new BigDecimal(500), "Aldo", "Aldo",
            TipoMovimiento.GASTO, TipoDivision.PERSONALIZADA, LocalDateTime.now(),
            List.of(new ParticipacionResponse(1L, "Aldo", new BigDecimal(500))));

        when(movimientoService.obtenerDetalleMovimiento(1L, 1L)).thenReturn(response);

        mockMvc.perform(
            get("/movimientos/grupos/{grupoId}/movimientos/{movimientoId}", grupoId, movimientoId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.tipo").value("GASTO"));
    }
}
