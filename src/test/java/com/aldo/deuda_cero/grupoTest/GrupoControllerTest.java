package com.aldo.deuda_cero.grupoTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.deuda_cero.controller.GrupoController;
import com.aldo.deuda_cero.dto.grupo.GrupoRequest;
import com.aldo.deuda_cero.dto.grupo.GrupoResponse;
import com.aldo.deuda_cero.dto.grupo.MiembrosResponse;
import com.aldo.deuda_cero.security.CustomUserDetailsService;
import com.aldo.deuda_cero.security.JwtService;
import com.aldo.deuda_cero.service.interfaces.GrupoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(GrupoController.class)
public class GrupoControllerTest {
    
    @MockitoBean
    private GrupoService grupoService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void deberiaCrearGrupoCorrectamente() throws Exception {

        GrupoRequest request = new GrupoRequest();
        request.setNombre("Grupo test");
        request.setDescripcion("Grupo para realizar test");

        GrupoResponse response = new GrupoResponse(1L, request.getNombre(), request.getDescripcion(), 1L);

        when(grupoService.crearGrupo(any(GrupoRequest.class))).thenReturn(response);

        mockMvc.perform(
            post("/grupos/crear")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.descripcion").value("Grupo para realizar test"));

    }

    @Test
    @WithMockUser
    void deberiaObtenerMisGrupos() throws Exception {

        List<GrupoResponse> response = List.of(
            new GrupoResponse(1L, "Grupo test", "Grupo para realizar test", 3L)
        );

        when(grupoService.obtenerMisGrupos()).thenReturn(response);

        mockMvc.perform(
            get("/grupos/grupos")
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].nombre").value("Grupo test"))
        .andExpect(jsonPath("$[0].totalMiembros").value(3L));

    }

    @Test
    @WithMockUser
    void deberiaObtenerMiembros() throws Exception {

        Long grupoId = 1L;

        List<MiembrosResponse> response = List.of(
            new MiembrosResponse(2L, "Aldo", "MIEMBRO")
        );

        when(grupoService.obtenerMiembros(grupoId)).thenReturn(response);

        mockMvc.perform(
            get("/grupos/{grupoId}/miembros", grupoId)
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].nombre").value("Aldo"))
        .andExpect(jsonPath("$[0].id").value(2L));

    }

}
