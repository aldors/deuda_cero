package com.aldo.deuda_cero.movimientoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.aldo.deuda_cero.dto.Movimientos.CrearMovimientoRequest;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoDetalleResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResumenResponse;
import com.aldo.deuda_cero.dto.Movimientos.ParticipanteMovimientoRequest;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Movimiento;
import com.aldo.deuda_cero.entity.ParticipacionMovimiento;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.entity.enums.Role;
import com.aldo.deuda_cero.entity.enums.TipoDivision;
import com.aldo.deuda_cero.entity.enums.TipoMovimiento;
import com.aldo.deuda_cero.exception.MiembroPagadorNoPerteneceAlGrupoException;
import com.aldo.deuda_cero.exception.MovimientoNoEncontradoException;
import com.aldo.deuda_cero.exception.ParticipantesNoPertenecenAlGrupoException;
import com.aldo.deuda_cero.exception.SumaDeMontosNoCoincideConMontoTotalException;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.repository.MovimientoRepository;
import com.aldo.deuda_cero.repository.ParticipacionMovimientoRepository;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.implementaciones.MovimientoServiceImpl;
import com.aldo.deuda_cero.service.interfaces.DeudaService;

@ExtendWith(MockitoExtension.class)
public class MovimientoServiceTest {

    @Mock
    private GroupPermissionService groupPermissionService;

    @Mock
    private MiembroGrupoRepository miembroGrupoRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private ParticipacionMovimientoRepository participacionMovimientoRepository;

    @Mock
    private DeudaService deudaService;

    @InjectMocks
    private MovimientoServiceImpl movimientoServiceImpl;

    private Usuario usuario;
    private MiembroGrupo miembroGrupo;
    private Movimiento movimiento;
    private Grupo grupo;
    private CrearMovimientoRequest crearMovimientoRequest;
    private ParticipanteMovimientoRequest participanteMovimientoRequest;

    @BeforeEach
    void setUp(){

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

        grupo = Grupo.builder()
            .id(1L)
            .nombre("Grupo test")
            .descripcion("Este es un grupo para realizar test")
            .fechaCreacion(LocalDateTime.now())
            .build();

        List<ParticipacionMovimiento> participaciones = List.of(
            new ParticipacionMovimiento(1L, movimiento, miembroGrupo, new BigDecimal(500))
        );

        movimiento = Movimiento.builder()
            .id(1L)
            .grupo(grupo)
            .registradoPor(miembroGrupo)
            .pagador(miembroGrupo)
            .tipo(TipoMovimiento.GASTO)
            .descripcion("Gasto test")
            .montoTotal(BigDecimal.valueOf(500))
            .fechaMovimiento(LocalDateTime.now())
            .fechaCreacion(LocalDateTime.now())
            .tipoDivision(TipoDivision.PERSONALIZADA)
            .participaciones(participaciones)
            .build();

        participanteMovimientoRequest = new ParticipanteMovimientoRequest();
        participanteMovimientoRequest.setUsuarioId(1L);
        participanteMovimientoRequest.setMonto(BigDecimal.valueOf(500));

        List<ParticipanteMovimientoRequest> participantes = List.of(
            participanteMovimientoRequest 
        );

        crearMovimientoRequest = new CrearMovimientoRequest();
        crearMovimientoRequest.setDescripcion("Gasto test");
        crearMovimientoRequest.setMontoTotal(BigDecimal.valueOf(500));
        crearMovimientoRequest.setPagadorId(1L);
        crearMovimientoRequest.setTipoMovimiento(TipoMovimiento.GASTO);
        crearMovimientoRequest.setTipoDivision(TipoDivision.PERSONALIZADA);
        crearMovimientoRequest.setParticipantes(participantes);

        Authentication auth = new UsernamePasswordAuthenticationToken("test@test.com", null, List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    /* Happy path */
    @Test
    void deberiaCrearMovimientoCorrectamente(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupo);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(1L, 1L)).thenReturn(Optional.of(miembroGrupo));
        when(miembroGrupoRepository.findByGrupoIdAndUsuarioIdIn(1L, List.of(1L))).thenReturn(List.of(miembroGrupo));
        when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
        when(participacionMovimientoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MovimientoResponse response = movimientoServiceImpl.crearMovimiento(grupo.getId(), crearMovimientoRequest);

        assertNotNull(response);
        assertEquals(crearMovimientoRequest.getMontoTotal(), response.getMontoTotal());
        assertEquals(crearMovimientoRequest.getDescripcion(), response.getDescripcion());

        verify(movimientoRepository).save(any(Movimiento.class));
    }

    @Test
    void deberiaObtenerMovimientosCorrectamente(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupo);
        when(movimientoRepository.findByGrupoIdOrderByFechaMovimientoDesc(grupo.getId())).thenReturn(List.of(movimiento));

        List<MovimientoResumenResponse> response = movimientoServiceImpl.obtenerMovimientos(grupo.getId());

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(movimiento.getDescripcion(), response.get(0).getDescripcion());
    }

    @Test
    void deberiaObtenerDetalleMovimientoCorrectamente(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupo);
        when(movimientoRepository.findByIdAndGrupoId(movimiento.getId(), grupo.getId())).thenReturn(Optional.of(movimiento));

        MovimientoDetalleResponse response = movimientoServiceImpl.obtenerDetalleMovimiento(grupo.getId(), movimiento.getId());

        assertNotNull(response);
        assertEquals(movimiento.getId(), response.getId());
        assertEquals(movimiento.getTipo(), response.getTipo());
    }


    /* Error path */
    @Test
    void deberiaLanzarExcepcionSiElMiembroPagadorNoPerteneceAlGrupo(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupo);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario.getId(), grupo.getId())).thenReturn(Optional.empty());

        assertThrows(MiembroPagadorNoPerteneceAlGrupoException.class, () -> movimientoServiceImpl.crearMovimiento(grupo.getId(), crearMovimientoRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiLosParticipantesNoPertenecenAlGrupo(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupo);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupo));
        when(miembroGrupoRepository.findByGrupoIdAndUsuarioIdIn(1L, List.of(1L))).thenReturn(List.of());

        assertThrows(ParticipantesNoPertenecenAlGrupoException.class, () -> movimientoServiceImpl.crearMovimiento(grupo.getId(), crearMovimientoRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiSumaDeMontosNoCoincideConMontoTotal(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupo);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupo));
        when(miembroGrupoRepository.findByGrupoIdAndUsuarioIdIn(1L, List.of(1L))).thenReturn(List.of(miembroGrupo));

        crearMovimientoRequest.setMontoTotal(new BigDecimal(0));

        assertThrows(SumaDeMontosNoCoincideConMontoTotalException.class, () -> movimientoServiceImpl.crearMovimiento(grupo.getId(), crearMovimientoRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiMovimientoNoEncontrado(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupo);
        when(movimientoRepository.findByIdAndGrupoId(movimiento.getId(), grupo.getId())).thenReturn(Optional.empty());

        assertThrows(MovimientoNoEncontradoException.class, () -> movimientoServiceImpl.obtenerDetalleMovimiento(grupo.getId(), movimiento.getId()));
    }

}