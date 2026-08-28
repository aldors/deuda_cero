package com.aldo.deuda_cero.balanceTest;

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

import com.aldo.deuda_cero.dto.balance.BalanceMiembroResponse;
import com.aldo.deuda_cero.dto.balance.TotalConsumidoResponse;
import com.aldo.deuda_cero.dto.balance.TotalPagadoResponse;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.entity.enums.Role;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.repository.MovimientoRepository;
import com.aldo.deuda_cero.repository.ParticipacionMovimientoRepository;
import com.aldo.deuda_cero.service.implementaciones.BalanceServiceImpl;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceTest {

    @Mock
    private MiembroGrupoRepository miembroGrupoRepository;

    @Mock
    private ParticipacionMovimientoRepository participacionMovimientoRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private BalanceServiceImpl balanceServiceImpl;

    private MiembroGrupo miembroGrupo;
    private Grupo grupo;
    private Usuario usuario;

    @BeforeEach
    void setUp(){

        usuario = Usuario.builder()
            .id(1L)
            .nombre("Aldo")
            .apellido("Test")
            .email("aldo@test.com")
            .password("test321")
            .activo(true)
            .fechaCreacion(LocalDateTime.now())
            .fechaActualizacion(LocalDateTime.now())
            .role(Role.USER)
            .build();

        grupo = Grupo.builder()
            .id(1L)
            .nombre("Grupo test")
            .descripcion("Grupo para realizar test")
            .fechaCreacion(LocalDateTime.now())
            .build();
        
        miembroGrupo = MiembroGrupo.builder()
            .id(1L)
            .usuario(usuario)
            .grupo(grupo)
            .rol(RolGrupo.MIEMBRO)
            .estado(EstadoMiembro.ACTIVO)
            .fechaIngreso(LocalDateTime.now())
            .build();

    }

    /*Happy path */
    @Test
    void deberiaObtenerBalanceCorrectamente(){

        List<TotalPagadoResponse> totalPagado = List.of(
            new TotalPagadoResponse(miembroGrupo.getId(), new BigDecimal(300))
        );

        List<TotalConsumidoResponse> totalConsumido = List.of(
            new TotalConsumidoResponse(miembroGrupo.getId(), new BigDecimal(200))
        );

        when(miembroGrupoRepository.findByGrupoIdAndEstado(grupo.getId(), EstadoMiembro.ACTIVO)).thenReturn(List.of(miembroGrupo));
        when(movimientoRepository.obtenerTotalPagadoPorMiembro(grupo.getId())).thenReturn(totalPagado);
        when(participacionMovimientoRepository.obtenerTotalConsumidoPorMiembro(grupo.getId())).thenReturn(totalConsumido);

        List<BalanceMiembroResponse> response = balanceServiceImpl.obtenerBalance(grupo.getId());

        assertNotNull(response);
        assertEquals(miembroGrupo.getUsuario().getNombre(), response.get(0).getNombre());
        assertEquals(miembroGrupo.getId(), response.get(0).getMiembroGrupoId());
        assertEquals(new BigDecimal(100), response.get(0).getBalance());
    }


    /*Error path */
    @Test
    void deberiaRetornarListaVaciaCuandoNoHayMiembrosActivosCorrectamente(){

        when(miembroGrupoRepository.findByGrupoIdAndEstado(grupo.getId(), EstadoMiembro.ACTIVO)).thenReturn(List.of());

        List<BalanceMiembroResponse> response = balanceServiceImpl.obtenerBalance(grupo.getId());

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    
    /*Edge case */
    @Test
    void deberiaObtenerBalanceCeroCandoNoHayMovimientosCorrectamente(){

        when(miembroGrupoRepository.findByGrupoIdAndEstado(grupo.getId(), EstadoMiembro.ACTIVO)).thenReturn(List.of(miembroGrupo));
        when(movimientoRepository.obtenerTotalPagadoPorMiembro(grupo.getId())).thenReturn(List.of());
        when(participacionMovimientoRepository.obtenerTotalConsumidoPorMiembro(grupo.getId())).thenReturn(List.of());

        List<BalanceMiembroResponse> response = balanceServiceImpl.obtenerBalance(grupo.getId());

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.get(0).getBalance());
    }

    @Test
    void deberiaCalcularBalanceSoloConPagosCorrectamente(){

        List<TotalPagadoResponse> totalPagado = List.of(
            new TotalPagadoResponse(miembroGrupo.getId(), new BigDecimal(300))
        );

        when(miembroGrupoRepository.findByGrupoIdAndEstado(grupo.getId(), EstadoMiembro.ACTIVO)).thenReturn(List.of(miembroGrupo));
        when(movimientoRepository.obtenerTotalPagadoPorMiembro(grupo.getId())).thenReturn(totalPagado);
        when(participacionMovimientoRepository.obtenerTotalConsumidoPorMiembro(grupo.getId())).thenReturn(List.of());

        List<BalanceMiembroResponse> response = balanceServiceImpl.obtenerBalance(grupo.getId());

        assertNotNull(response);
        assertEquals(miembroGrupo.getUsuario().getNombre(), response.get(0).getNombre());
        assertEquals(miembroGrupo.getId(), response.get(0).getMiembroGrupoId());
        assertEquals(new BigDecimal(300), response.get(0).getBalance());
    }

    @Test
    void deberiaCalcularBalanceNegativoCuandoSoloHayConsumos(){

        List<TotalConsumidoResponse> totalConsumido = List.of(
            new TotalConsumidoResponse(miembroGrupo.getId(), new BigDecimal(200))
        );

        when(miembroGrupoRepository.findByGrupoIdAndEstado(grupo.getId(), EstadoMiembro.ACTIVO)).thenReturn(List.of(miembroGrupo));
        when(movimientoRepository.obtenerTotalPagadoPorMiembro(grupo.getId())).thenReturn(List.of());
        when(participacionMovimientoRepository.obtenerTotalConsumidoPorMiembro(grupo.getId())).thenReturn(totalConsumido);

        List<BalanceMiembroResponse> response = balanceServiceImpl.obtenerBalance(grupo.getId());

        assertNotNull(response);
        assertEquals(miembroGrupo.getUsuario().getNombre(), response.get(0).getNombre());
        assertEquals(miembroGrupo.getId(), response.get(0).getMiembroGrupoId());
        assertEquals(new BigDecimal(-200), response.get(0).getBalance());
    }

}
