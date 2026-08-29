package com.aldo.deuda_cero.pagoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.aldo.deuda_cero.dto.pagos.RegistrarPagoRequest;
import com.aldo.deuda_cero.dto.pagos.PagoResponse;
import com.aldo.deuda_cero.entity.Deuda;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.PagoDeuda;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoDeuda;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.entity.enums.Role;
import com.aldo.deuda_cero.exception.DeudaNoEncontradaException;
import com.aldo.deuda_cero.exception.DeudaNoPerteneceAlGrupoException;
import com.aldo.deuda_cero.exception.DeudaSaldadaException;
import com.aldo.deuda_cero.exception.GrupoNoEncontradoException;
import com.aldo.deuda_cero.exception.MiembroPagadorNoPerteneceAlGrupoException;
import com.aldo.deuda_cero.exception.MontoAPagarDebeSerMayorQueCeroException;
import com.aldo.deuda_cero.exception.NoPuedesPagarUnaDeudaQueNoTePerteneceException;
import com.aldo.deuda_cero.exception.PagoNoPuedeSuperarLaDeudaPendiente;
import com.aldo.deuda_cero.repository.DeudaRepository;
import com.aldo.deuda_cero.repository.GrupoRepository;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.repository.PagoDeudaRepository;
import com.aldo.deuda_cero.security.CurrentUserService;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.implementaciones.PagoDeudaServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PagoDeudaTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private MiembroGrupoRepository miembroGrupoRepository;

    @Mock
    private PagoDeudaRepository pagoDeudaRepository;

    @Mock
    private DeudaRepository deudaRepository;

    @Mock
    private GroupPermissionService groupPermissionService;

    @InjectMocks
    private PagoDeudaServiceImpl pagoDeudaServiceImpl;

    private Grupo grupo;
    private Usuario usuario1;
    private Usuario usuario2;
    private MiembroGrupo miembroGrupoDeudor;
    private MiembroGrupo miembroGrupoAcreedor;
    private Deuda deuda;
    private PagoDeuda pagoDeuda;
    private RegistrarPagoRequest registrarPagoRequest;

    @BeforeEach
    void setUp(){

        grupo = Grupo.builder()
            .id(1L)
            .nombre("Grupo test")
            .descripcion("Grupo para realizar tests")
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
            .password("t3st123")
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

        pagoDeuda = PagoDeuda.builder()
            .id(1L)
            .deuda(deuda)
            .monto(new BigDecimal(500))
            .fechaPago(LocalDateTime.now())
            .fechaCreacion(LocalDateTime.now())
            .build();

        registrarPagoRequest = new RegistrarPagoRequest();
        registrarPagoRequest.setDeudaId(deuda.getId());
        registrarPagoRequest.setMonto(deuda.getMontoPendiente());

    }

    /*Happy path */
    //Este test funciona como deberiaRegistrarPagoYSaldarCompletamenteLaDeudaCorrectamente
    @Test
    void deberiaRegistrarPagoCorrectamente(){

        when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario1);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario1.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupoDeudor));
        when(deudaRepository.findById(registrarPagoRequest.getDeudaId())).thenReturn(Optional.of(deuda));
        when(pagoDeudaRepository.save(any(PagoDeuda.class))).thenReturn(pagoDeuda);
        when(deudaRepository.save(any(Deuda.class))).thenReturn(deuda);

        pagoDeudaServiceImpl.registrarPago(grupo.getId(), registrarPagoRequest);

        verify(pagoDeudaRepository).save(any(PagoDeuda.class));
        verify(deudaRepository).save(any(Deuda.class));
        
        assertEquals(BigDecimal.ZERO, deuda.getMontoPendiente());
        assertEquals(EstadoDeuda.SALDADA, deuda.getEstado());

    }

    @Test
    void deberiaRegistrarPagoYNoSaldarCompletamenteLaDeudaCorrectamente(){

        when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario1);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario1.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupoDeudor));
        when(deudaRepository.findById(registrarPagoRequest.getDeudaId())).thenReturn(Optional.of(deuda));
        when(pagoDeudaRepository.save(any(PagoDeuda.class))).thenReturn(pagoDeuda);
        when(deudaRepository.save(any(Deuda.class))).thenReturn(deuda);

        registrarPagoRequest.setMonto(new BigDecimal(100));

        pagoDeudaServiceImpl.registrarPago(grupo.getId(), registrarPagoRequest);

        verify(pagoDeudaRepository).save(any(PagoDeuda.class));
        verify(deudaRepository).save(any(Deuda.class));
        
        assertEquals(new BigDecimal(100), deuda.getMontoPendiente());
        assertEquals(EstadoDeuda.PENDIENTE, deuda.getEstado());

    }

    @Test
    void deberiaObtenerPagos(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupoDeudor);
        when(pagoDeudaRepository.findByDeudaGrupoIdOrderByFechaPagoDesc(grupo.getId())).thenReturn(List.of(pagoDeuda));

        List<PagoResponse> response = pagoDeudaServiceImpl.obtenerPagos(grupo.getId());

        assertNotNull(response);
        assertEquals(pagoDeuda.getDeuda().getDeudor().getUsuario().getNombre(), response.get(0).getPagador());
        assertEquals(pagoDeuda.getDeuda().getAcreedor().getUsuario().getNombre(), response.get(0).getReceptor());
    }


    /*Error path */
    @Test
    void dberiaLnzarExcepcionSiGrupoNoEncontrado(){

        when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.empty());

        assertThrows(GrupoNoEncontradoException.class, () -> pagoDeudaServiceImpl.registrarPago(grupo.getId(), registrarPagoRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiMiembroPagadorNoPerteneceAlGrupo(){
        
        when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario1);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario1.getId(), grupo.getId())).thenReturn(Optional.empty());

        assertThrows(MiembroPagadorNoPerteneceAlGrupoException.class, () -> pagoDeudaServiceImpl.registrarPago(grupo.getId(), registrarPagoRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiMontoAPagarNoEsMayorQueCero(){

        // También se puede probar con null
        registrarPagoRequest.setMonto(BigDecimal.ZERO);

        when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario1);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario1.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupoDeudor));

        assertThrows(MontoAPagarDebeSerMayorQueCeroException.class, () -> pagoDeudaServiceImpl.registrarPago(grupo.getId(), registrarPagoRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiDeudaNoEncontrada(){

        when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario1);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario1.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupoDeudor));
        when(deudaRepository.findById(registrarPagoRequest.getDeudaId())).thenReturn(Optional.empty());

        assertThrows(DeudaNoEncontradaException.class, () -> pagoDeudaServiceImpl.registrarPago(grupo.getId(), registrarPagoRequest));
    }

    @Test
    void deberiaLanzarExcpecionSiDeudaNoPerteneceAlGrupo(){

        when(grupoRepository.findById(anyLong())).thenReturn(Optional.of(grupo));
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario1);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(anyLong(), anyLong())).thenReturn(Optional.of(miembroGrupoDeudor));
        when(deudaRepository.findById(registrarPagoRequest.getDeudaId())).thenReturn(Optional.of(deuda));

        assertThrows(DeudaNoPerteneceAlGrupoException.class, () -> pagoDeudaServiceImpl.registrarPago(10L, registrarPagoRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiIntentasPagarUnaDeudaQueNoTePertenece(){

        // miembroGrupoAcreedor es el usuario 2 con id 2L
        deuda.setDeudor(miembroGrupoAcreedor);

        when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario1);

        // miembroGrupoDeudor es usuario 1 con id 1L
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario1.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupoDeudor));
        when(deudaRepository.findById(registrarPagoRequest.getDeudaId())).thenReturn(Optional.of(deuda));

        // Aquí 2L != 1L osea el id del deudor (usuario 2) es diferente al id del pagador (usuario 1)
        assertThrows(NoPuedesPagarUnaDeudaQueNoTePerteneceException.class, () -> pagoDeudaServiceImpl.registrarPago(grupo.getId(), registrarPagoRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiLaDeudaYaFueSaldada(){

        deuda.setEstado(EstadoDeuda.SALDADA);

        when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario1);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario1.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupoDeudor));
        when(deudaRepository.findById(registrarPagoRequest.getDeudaId())).thenReturn(Optional.of(deuda));

        assertThrows(DeudaSaldadaException.class, () -> pagoDeudaServiceImpl.registrarPago(grupo.getId(), registrarPagoRequest));

    }
    
    @Test
    void deberiaLanzarExcepcionSiIntentasPagarMasDeLaDeudaPendiente(){

        registrarPagoRequest.setMonto(new BigDecimal(1000));

        when(grupoRepository.findById(grupo.getId())).thenReturn(Optional.of(grupo));
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario1);
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario1.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupoDeudor));
        when(deudaRepository.findById(registrarPagoRequest.getDeudaId())).thenReturn(Optional.of(deuda));

        assertThrows(PagoNoPuedeSuperarLaDeudaPendiente.class, () -> pagoDeudaServiceImpl.registrarPago(grupo.getId(), registrarPagoRequest));
    }

    @Test
    void deberiaRetornarListaVaciaAlgoSiNoHayPagos(){

        when(groupPermissionService.obtenerMiembroActual(grupo.getId())).thenReturn(miembroGrupoDeudor);
        when(pagoDeudaRepository.findByDeudaGrupoIdOrderByFechaPagoDesc(grupo.getId())).thenReturn(List.of());

        List<PagoResponse> response = pagoDeudaServiceImpl.obtenerPagos(grupo.getId());

        assertNotNull(response);
        assertTrue(response.isEmpty());

    }
}
