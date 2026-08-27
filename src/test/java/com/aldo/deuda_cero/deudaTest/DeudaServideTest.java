package com.aldo.deuda_cero.deudaTest;

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

import com.aldo.deuda_cero.dto.deuda.DeudaResponse;
import com.aldo.deuda_cero.dto.deuda.ImpactoDeudaResponse;
import com.aldo.deuda_cero.entity.Deuda;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoDeuda;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.entity.enums.Role;
import com.aldo.deuda_cero.exception.DeudaNoEncontradaException;
import com.aldo.deuda_cero.exception.DeudaNoPerteneceAlGrupoException;
import com.aldo.deuda_cero.repository.DeudaRepository;
import com.aldo.deuda_cero.service.implementaciones.DeudaServiceImpl;

@ExtendWith(MockitoExtension.class)
public class DeudaServideTest {
    
    @Mock
    private DeudaRepository deudaRepository;

    @InjectMocks
    private DeudaServiceImpl deudaServiceImpl;

    private Grupo grupo;
    private Usuario usuario1;
    private Usuario usuario2;
    private Deuda deuda;
    private MiembroGrupo miembroGrupoDeudor;
    private MiembroGrupo miembroGrupoAcreedor;

    @BeforeEach
    void setUp(){

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

        usuario2 = Usuario.builder()
            .id(2L)
            .nombre("Odla")
            .apellido("Test")
            .email("odla@test.com")
            .password("t35t")
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

        miembroGrupoAcreedor = MiembroGrupo.builder()
            .id(2L)
            .usuario(usuario2)
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

        deuda = Deuda.builder()
            .id(1L)
            .grupo(grupo)
            .deudor(miembroGrupoDeudor)
            .acreedor(miembroGrupoAcreedor)
            .montoOriginal(new BigDecimal(500))
            .montoPendiente(new BigDecimal(250))
            .estado(EstadoDeuda.PENDIENTE)
            .fechaCreacion(LocalDateTime.now())
            .fechaActualizacion(LocalDateTime.now())
            .build();

    }

    /*Happy path */
    @Test
    void deberiaObtenerDeudasCorrectamente(){

        when(deudaRepository.findByGrupoId(grupo.getId())).thenReturn(List.of(deuda));

        List<DeudaResponse> response = deudaServiceImpl.obtenerDeudas(grupo.getId());

        assertNotNull(response);
        assertEquals(miembroGrupoDeudor.getId(), response.get(0).getDeudorId());
        assertEquals(miembroGrupoAcreedor.getId(), response.get(0).getAcreedorId());
    }

    @Test
    void deberiaObtenerDeudasPendientesCorrectamente(){

        when(deudaRepository.findByGrupoIdAndEstado(grupo.getId(), EstadoDeuda.PENDIENTE)).thenReturn(List.of(deuda));

        List<DeudaResponse> response = deudaServiceImpl.obtenerDeudasPendientes(grupo.getId());

        assertNotNull(response);
        assertEquals(miembroGrupoDeudor.getId(), response.get(0).getDeudorId());
        assertEquals(miembroGrupoAcreedor.getId(), response.get(0).getAcreedorId());
    }

    @Test
    void deberiaObtenerDeudaPorIdCorrectamente(){

        when(deudaRepository.findById(grupo.getId())).thenReturn(Optional.of(deuda));

        DeudaResponse response = deudaServiceImpl.obtenerDeudaPorId(grupo.getId(), deuda.getId());

        assertNotNull(response);
        assertEquals(miembroGrupoDeudor.getId(), response.getDeudorId());
        assertEquals(miembroGrupoAcreedor.getId(), response.getAcreedorId());
    }

    @Test
    void deberiaPorcesarImpactosCreandoNuevaDeudaCorrectamente(){

        ImpactoDeudaResponse impactoDeuda = new ImpactoDeudaResponse(miembroGrupoDeudor, miembroGrupoAcreedor, new BigDecimal(500));

        when(deudaRepository.findByGrupoIdAndDeudorIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), miembroGrupoAcreedor.getId(), EstadoDeuda.PENDIENTE))
            .thenReturn(Optional.empty());

        when(deudaRepository.findByGrupoIdAndDeudorIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoAcreedor.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE))
            .thenReturn(Optional.empty());

        deudaServiceImpl.procesarImpactos(grupo.getId(), List.of(impactoDeuda));

        verify(deudaRepository).save(any(Deuda.class));
    }

    @Test
    void deberiaProcesarImpactosAumentandoUnaDeudaExistenteCorrectamente(){

        ImpactoDeudaResponse impactoDeuda = new ImpactoDeudaResponse(miembroGrupoDeudor, miembroGrupoAcreedor, new BigDecimal(500));

        when(deudaRepository.findByGrupoIdAndDeudorIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), miembroGrupoAcreedor.getId(), EstadoDeuda.PENDIENTE))
            .thenReturn(Optional.of(deuda));

        deudaServiceImpl.procesarImpactos(grupo.getId(), List.of(impactoDeuda));

        assertEquals(new BigDecimal(750), deuda.getMontoPendiente());
    }

    @Test
    void deberiaProcesarImpactosCompensandoDeudaInversa(){

        ImpactoDeudaResponse impactoDeuda = new ImpactoDeudaResponse(miembroGrupoDeudor, miembroGrupoAcreedor, new BigDecimal(250));

        when(deudaRepository.findByGrupoIdAndDeudorIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoDeudor.getId(), miembroGrupoAcreedor.getId(), EstadoDeuda.PENDIENTE))
            .thenReturn(Optional.empty());

        when(deudaRepository.findByGrupoIdAndDeudorIdAndAcreedorIdAndEstado(grupo.getId(), miembroGrupoAcreedor.getId(), miembroGrupoDeudor.getId(), EstadoDeuda.PENDIENTE))
            .thenReturn(Optional.of(deuda));

        deudaServiceImpl.procesarImpactos(grupo.getId(), List.of(impactoDeuda));

        assertEquals(BigDecimal.ZERO, deuda.getMontoPendiente());
        assertEquals(EstadoDeuda.SALDADA, deuda.getEstado());
        verify(deudaRepository).save(any(Deuda.class));
    }


    /*Error path */
    @Test
    void deberiaLanzarExcepcionSiDeudaNoEncontrada(){

        when(deudaRepository.findById(grupo.getId())).thenReturn(Optional.empty());

        assertThrows(DeudaNoEncontradaException.class, () -> deudaServiceImpl.obtenerDeudaPorId(grupo.getId(), deuda.getId()));
    }

    @Test
    void deberiaLanzarExcepcionSiDeudaNoPerteneceAlGrupo(){

        when(deudaRepository.findById(grupo.getId())).thenReturn(Optional.of(deuda));

        assertThrows(DeudaNoPerteneceAlGrupoException.class, () -> deudaServiceImpl.obtenerDeudaPorId(2L, deuda.getId()));
    }

}
