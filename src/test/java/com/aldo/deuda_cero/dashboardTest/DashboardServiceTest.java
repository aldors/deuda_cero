package com.aldo.deuda_cero.dashboardTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aldo.deuda_cero.dto.dashboard.DashboardResponse;
import com.aldo.deuda_cero.entity.Deuda;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Movimiento;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoDeuda;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.entity.enums.Role;
import com.aldo.deuda_cero.entity.enums.TipoDivision;
import com.aldo.deuda_cero.entity.enums.TipoMovimiento;
import com.aldo.deuda_cero.repository.DeudaRepository;
import com.aldo.deuda_cero.repository.MovimientoRepository;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.implementaciones.DashboardServiceImpl;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {
    
    @Mock
    private GroupPermissionService groupPermissionService;

    @Mock
    private DeudaRepository deudaRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardServiceImpl;

    private Usuario usuario1;
    private Usuario usuario2;
    private Grupo grupo;
    private MiembroGrupo miembroGrupoDeudor;
    private MiembroGrupo miembroGrupoAcreedor;
    private Deuda deuda;
    private Deuda acreencia;
    private Movimiento movimiento;

    @BeforeEach
    void setUp(){

        grupo = Grupo.builder()
            .id(1L)
            .nombre("Grupo test")
            .descripcion("Este es un grupo para realizar test")
            .fechaCreacion(LocalDateTime.now())
            .build();

        usuario1 = Usuario.builder()
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

        miembroGrupoDeudor = MiembroGrupo.builder()
            .id(1L)
            .usuario(usuario1)
            .grupo(grupo)
            .rol(RolGrupo.MIEMBRO)
            .estado(EstadoMiembro.ACTIVO)
            .fechaIngreso(LocalDateTime.now())
            .build();

        usuario2 = Usuario.builder()
            .id(2L)
            .nombre("Odla")
            .apellido("Test")
            .email("odla@test.com")
            .password("test456")
            .activo(true)
            .fechaCreacion(LocalDateTime.now())
            .fechaActualizacion(LocalDateTime.now())
            .role(Role.USER)
            .build();

        miembroGrupoAcreedor = MiembroGrupo.builder()
            .id(2L)
            .usuario(usuario2)
            .grupo(grupo)
            .rol(RolGrupo.MIEMBRO)
            .estado(EstadoMiembro.ACTIVO)
            .fechaIngreso(LocalDateTime.now())
            .build();

        deuda = Deuda.builder()
            .id(1L)
            .grupo(grupo)
            .deudor(miembroGrupoDeudor)
            .acreedor(miembroGrupoAcreedor)
            .montoOriginal(new BigDecimal(500))
            .montoPendiente(new BigDecimal(200))
            .estado(EstadoDeuda.PENDIENTE)
            .fechaCreacion(LocalDateTime.now())
            .fechaActualizacion(LocalDateTime.now())
            .build();

        acreencia = Deuda.builder()
            .id(2L)
            .grupo(grupo)
            .deudor(miembroGrupoAcreedor)
            .acreedor(miembroGrupoDeudor)
            .montoOriginal(new BigDecimal(1000))
            .montoPendiente(new BigDecimal(500))
            .estado(EstadoDeuda.PENDIENTE)
            .fechaCreacion(LocalDateTime.now())
            .fechaActualizacion(LocalDateTime.now())
            .build();

        movimiento = Movimiento.builder()
            .id(1L)
            .grupo(grupo)
            .registradoPor(miembroGrupoDeudor)
            .pagador(miembroGrupoDeudor)
            .tipo(TipoMovimiento.GASTO)
            .descripcion("Movimiento test")
            .montoTotal(new BigDecimal(500))
            .fechaMovimiento(LocalDateTime.now())
            .fechaCreacion(LocalDateTime.now())
            .tipoDivision(TipoDivision.PERSONALIZADA)
            .build();
    }

    /*Happy path */
    @Test
    void deberiaObtenerDashboardCorrectamente(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupoDeudor);
        when(deudaRepository.findByGrupoIdAndDeudorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of(deuda));
        when(deudaRepository.findByGrupoIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of(acreencia));

        DashboardResponse response = dashboardServiceImpl.obtenerDashboard(grupo.getId());

        assertNotNull(response);
        assertEquals(new BigDecimal(300), response.getBalanceGeneral());
    }

    @Test
    void deberiaConstruirResumenDeudas(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupoDeudor);
        when(deudaRepository.findByGrupoIdAndDeudorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of(deuda));
        when(deudaRepository.findByGrupoIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of());
        
        DashboardResponse response = dashboardServiceImpl.obtenerDashboard(grupo.getId());

        assertNotNull(response);
        assertEquals(usuario2.getNombre(), response.getDebes().get(0).getNombre());
        assertEquals(usuario2.getId(), response.getDebes().get(0).getUsuarioId());
        assertEquals(new BigDecimal(200), response.getDebes().get(0).getMontoPendiente());

    }

    @Test
    void deberiaObtenerMovimientosRecientesCorrectamente(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupoDeudor);
        when(deudaRepository.findByGrupoIdAndDeudorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of());
        when(deudaRepository.findByGrupoIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of());
        when(movimientoRepository.findTop10ByGrupoIdOrderByFechaMovimientoDesc(grupo.getId())).thenReturn(List.of(movimiento));

        DashboardResponse response = dashboardServiceImpl.obtenerDashboard(grupo.getId());

        assertNotNull(response);
        assertEquals(1, response.getMovimientosRecientes().size());
        assertEquals("Movimiento test", response.getMovimientosRecientes().get(0).getDescripcion());
        assertEquals(new BigDecimal(500), response.getMovimientosRecientes().get(0).getMontoTotal());

    }


    /*Error path */
    @Test
    void deberiaObtenerDashboardConListasVacias() {

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupoDeudor);
        when(deudaRepository.findByGrupoIdAndDeudorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of());
        when(deudaRepository.findByGrupoIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of());
        when(movimientoRepository.findTop10ByGrupoIdOrderByFechaMovimientoDesc(grupo.getId())).thenReturn(List.of());

        DashboardResponse response = dashboardServiceImpl.obtenerDashboard(grupo.getId());

        assertEquals(BigDecimal.ZERO, response.getBalanceGeneral());
        assertEquals(0, response.getDebes().size());
        assertEquals(0, response.getTeDeben().size());
        assertEquals(0, response.getMovimientosRecientes().size());
    }

    /*Edge case */
    @Test
    void deberiaObtenerDashboardConBalancePositivoCorrectamente(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupoDeudor);
        when(deudaRepository.findByGrupoIdAndDeudorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of());
        when(deudaRepository.findByGrupoIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of(acreencia));

        DashboardResponse response = dashboardServiceImpl.obtenerDashboard(grupo.getId());

        assertNotNull(response);
        assertEquals(new BigDecimal(500), response.getBalanceGeneral());
    }

    @Test
    void deberiaObtenerDashboardConBalanceNegativoCorrectamente(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupoDeudor);
        when(deudaRepository.findByGrupoIdAndDeudorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of(deuda));
        when(deudaRepository.findByGrupoIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of());

        DashboardResponse response = dashboardServiceImpl.obtenerDashboard(grupo.getId());

        assertNotNull(response);
        assertEquals(new BigDecimal(-200), response.getBalanceGeneral());
    }

    @Test
    void deberiaObtenerDashboardConBalanceCeroCuandoNoHayDeudasNiAcreencias() {

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupoDeudor);
        when(deudaRepository.findByGrupoIdAndDeudorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of());
        when(deudaRepository.findByGrupoIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of());

        DashboardResponse response = dashboardServiceImpl.obtenerDashboard(grupo.getId());

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getBalanceGeneral());
    }

}
