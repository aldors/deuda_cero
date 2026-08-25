package com.aldo.deuda_cero.grupoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aldo.deuda_cero.dto.grupo.GrupoRequest;
import com.aldo.deuda_cero.dto.grupo.GrupoResponse;
import com.aldo.deuda_cero.dto.grupo.MiembrosResponse;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.entity.enums.Role;
import com.aldo.deuda_cero.exception.GrupoNoEncontradoException;
import com.aldo.deuda_cero.repository.GrupoRepository;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.security.CurrentUserService;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.implementaciones.GrupoServiceImpl;

@ExtendWith(MockitoExtension.class)
public class GrupoServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GroupPermissionService groupPermissionService;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private MiembroGrupoRepository miembroGrupoRepository;

    @InjectMocks
    private GrupoServiceImpl grupoServiceImpl;

    private Grupo grupo;
    private Usuario usuario;
    private MiembroGrupo miembroGrupo;
    private GrupoRequest grupoRequest;
    
    @BeforeEach
    void setUp(){

        grupo = Grupo.builder()
            .id(1L)
            .nombre("Grupo test")
            .descripcion("Este es un grupo para realizar test")
            .fechaCreacion(LocalDateTime.now())
            .build();

        grupoRequest = new GrupoRequest();
        grupoRequest.setNombre("Grupo test");
        grupoRequest.setDescripcion("Este es un grupo para realizar test");

        usuario = Usuario.builder()
            .id(1L)
            .nombre("Aldo")
            .apellido("Test")
            .email("aldo@test.com")
            .password("test123")
            .activo(true)
            .fechaCreacion(LocalDateTime.now())
            .fechaActualizacion(LocalDateTime.now())
            .role(Role.USER)
            .build();

        miembroGrupo = MiembroGrupo.builder()
            .id(1L)
            .usuario(usuario)
            .grupo(grupo)
            .rol(RolGrupo.MIEMBRO)
            .estado(EstadoMiembro.ACTIVO)
            .fechaIngreso(LocalDateTime.now())
            .build();


        Authentication auth = new UsernamePasswordAuthenticationToken("test@test.com", null, List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    /*Happy Path*/
    @Test
    void deberiaCrearGrupoCorrectamente(){

        when(currentUserService.obtenerUsuarioActual()).thenReturn(new Usuario());
        when(grupoRepository.save(any(Grupo.class))).thenReturn(grupo);
        when(miembroGrupoRepository.save(any(MiembroGrupo.class))).thenReturn(miembroGrupo);

        GrupoResponse response = grupoServiceImpl.crearGrupo(grupoRequest);

        assertNotNull(response);
        assertEquals(grupoRequest.getNombre(), response.getNombre());
        assertEquals(grupoRequest.getDescripcion(), response.getDescripcion());

        verify(grupoRepository).save(any(Grupo.class));
    }

    @Test
    void deberiaObtenerMisGruposCorrectamente(){

        List<GrupoResponse> grupos = List.of(
            new GrupoResponse(1L, "Grupo A", "Grupo A test", 2L),
            new GrupoResponse(2L, "Grupo B", "Grupo B test", 5L)
        );

        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(miembroGrupoRepository.obtenerGruposDelUsuario(usuario.getId())).thenReturn(grupos);

        List<GrupoResponse> resultado = grupoServiceImpl.obtenerMisGrupos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Grupo B", resultado.get(1).getNombre());
    }

    @Test
    void deberiaObtenerMiembrosCorrectamente(){

        List<MiembroGrupo> miembros = List.of(
            miembroGrupo
        );

        when(grupoRepository.existsById(grupo.getId())).thenReturn(true);
        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupo);
        when(miembroGrupoRepository.findByGrupoIdAndEstado(grupo.getId(), EstadoMiembro.ACTIVO)).thenReturn(miembros);

        List<MiembrosResponse> resultado = grupoServiceImpl.obtenerMiembros(grupo.getId());

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Aldo", resultado.get(0).getNombre());
    }


    /*Error Path */
    @Test
    void deberiaLanzarExcepcionSiGrupoNoEncontrado(){

        when(grupoRepository.existsById(grupo.getId())).thenReturn(false);

        assertThrows(GrupoNoEncontradoException.class, () -> grupoServiceImpl.obtenerMiembros(grupo.getId()));
    }
}
