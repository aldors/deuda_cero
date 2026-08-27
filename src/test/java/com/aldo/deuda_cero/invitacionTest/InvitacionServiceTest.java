package com.aldo.deuda_cero.invitacionTest;

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

import com.aldo.deuda_cero.dto.invitaciones.AceptarInvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionesPendientesResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitarMiembrosRequest;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.Invitacion;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoInvitacion;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.entity.enums.Role;
import com.aldo.deuda_cero.exception.InvitacionATiMismoException;
import com.aldo.deuda_cero.exception.InvitacionNoEncontrada;
import com.aldo.deuda_cero.exception.InvitacionPendienteException;
import com.aldo.deuda_cero.exception.InvitacionRespondidaException;
import com.aldo.deuda_cero.exception.NoEsTuInvitacionException;
import com.aldo.deuda_cero.exception.UsuarioNoEncontradoException;
import com.aldo.deuda_cero.exception.UsuarioPerteneceAlGrupoException;
import com.aldo.deuda_cero.repository.InvitacionRepository;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.repository.UsuarioRepository;
import com.aldo.deuda_cero.security.CurrentUserService;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.implementaciones.InvitacionServiceImpl;


@ExtendWith(MockitoExtension.class)
public class InvitacionServiceTest {
    
    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private MiembroGrupoRepository miembroGrupoRepository;

    @Mock
    private GroupPermissionService groupPermissionService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private InvitacionRepository invitacionRepository;

    @InjectMocks
    private InvitacionServiceImpl invitacionServiceImpl;

    private InvitarMiembrosRequest invitarMiembrosRequest;
    private Grupo grupo;
    private Usuario usuario;
    private MiembroGrupo miembroGrupo;
    private Invitacion invitacion;
    private Usuario usuarioInvitado;

    @BeforeEach
    void setUp(){
    
        invitarMiembrosRequest = new InvitarMiembrosRequest("aldo@test.com");

        grupo = Grupo.builder()
            .id(1L)
            .nombre("Grupo invitación")
            .descripcion("Grupo invitación test")
            .fechaCreacion(LocalDateTime.now())
            .build();

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

        usuarioInvitado = Usuario.builder()
            .id(2L)
            .nombre("Otro usuario")
            .apellido("Test")
            .email("otro@test.com")
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

        invitacion = Invitacion.builder()
            .id(1L)
            .grupo(grupo)
            .invitado(usuarioInvitado)
            .invitador(usuario)
            .estado(EstadoInvitacion.PENDIENTE)
            .fechaEnvio(LocalDateTime.now())
            .build();
    }

    /*Happy path */
    @Test
    void deberiaInvitarUsuarioCorrectamente(){

        doNothing().when(groupPermissionService).validarAdministrador(grupo.getId());
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(usuarioRepository.findByEmail(invitarMiembrosRequest.getEmail())).thenReturn(Optional.of(usuarioInvitado));
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuarioInvitado.getId(), grupo.getId())).thenReturn(Optional.empty());
        when(invitacionRepository.existsByGrupo_IdAndInvitado_IdAndEstado(grupo.getId(), usuarioInvitado.getId(), EstadoInvitacion.PENDIENTE)).thenReturn(false);
        when(groupPermissionService.obtenerMiembro(grupo.getId(), usuario)).thenReturn(miembroGrupo);
        when(invitacionRepository.save(any(Invitacion.class))).thenReturn(invitacion);

        InvitacionResponse response = invitacionServiceImpl.invitarUsuarios(grupo.getId(), invitarMiembrosRequest);

        assertNotNull(response);
        assertEquals(invitacion.getId(), response.getId());
        assertEquals(invitacion.getGrupo().getNombre(), response.getGrupo());

        verify(invitacionRepository).save(any(Invitacion.class));

    }

    @Test
    void deberiaObtenerInvitacionesCorrectamente(){

        List<InvitacionesPendientesResponse> invitacionesPendientes = List.of(
            new InvitacionesPendientesResponse(1L, grupo.getNombre(), usuario.getNombre(), EstadoInvitacion.PENDIENTE, LocalDateTime.now())
        );

        when(currentUserService.obtenerIdUsuarioActual()).thenReturn(usuario.getId());
        when(invitacionRepository.obtenerInvitacionesPendientes(usuario.getId())).thenReturn(invitacionesPendientes);

        List<InvitacionesPendientesResponse> response = invitacionServiceImpl.obtenerInvitaciones();

        assertNotNull(response);
        assertEquals(grupo.getNombre(), response.get(0).getGrupo());
        assertEquals(usuario.getNombre(), response.get(0).getInvitador());
        
    }

    @Test
    void deberiaAceptarInvitacionCorrectamente(){
        
        // Simula la sesion del mismo usuario que acepta la invitación
        usuario.setId(usuarioInvitado.getId());

        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(invitacionRepository.findById(invitacion.getId())).thenReturn(Optional.of(invitacion));
        when(invitacionRepository.save(any(Invitacion.class))).thenReturn(invitacion);
        when(miembroGrupoRepository.save(any(MiembroGrupo.class))).thenReturn(miembroGrupo);

        AceptarInvitacionResponse response = invitacionServiceImpl.aceptarInvitacion(invitacion.getId());

        assertNotNull(response);
        assertEquals(invitacion.getGrupo().getNombre(), response.getGrupo());
        assertEquals(invitacion.getGrupo().getId(), response.getGrupoId());
        
        verify(invitacionRepository).save(any(Invitacion.class));
        verify(miembroGrupoRepository).save(any(MiembroGrupo.class));
        
    }

    @Test
    void deberiaRechazarInvitacionCorrectamente(){

        usuario.setId(usuarioInvitado.getId());

        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(invitacionRepository.findById(invitacion.getId())).thenReturn(Optional.of(invitacion));
        when(invitacionRepository.save(any(Invitacion.class))).thenReturn(invitacion);

        AceptarInvitacionResponse response = invitacionServiceImpl.rechazarInvitacion(invitacion.getId());

        assertNotNull(response);
        assertEquals(invitacion.getGrupo().getNombre(), response.getGrupo());
        assertEquals(invitacion.getGrupo().getId(), response.getGrupoId());
        
        verify(invitacionRepository).save(any(Invitacion.class));
    }


    /*Error path */
    @Test
    void deberiaLanzarExcepcionSiUsuarioNoEncontrado(){

        doNothing().when(groupPermissionService).validarAdministrador(grupo.getId());
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(usuarioRepository.findByEmail(invitarMiembrosRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> invitacionServiceImpl.invitarUsuarios(grupo.getId(), invitarMiembrosRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiTeInvitasATiMismo(){

        usuarioInvitado.setId(usuario.getId());

        doNothing().when(groupPermissionService).validarAdministrador(grupo.getId());
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(usuarioRepository.findByEmail(invitarMiembrosRequest.getEmail())).thenReturn(Optional.of(usuarioInvitado));

        assertThrows(InvitacionATiMismoException.class, () -> invitacionServiceImpl.invitarUsuarios(grupo.getId(), invitarMiembrosRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioYaPerteneceAlGrupo(){

        doNothing().when(groupPermissionService).validarAdministrador(grupo.getId());
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(usuarioRepository.findByEmail(invitarMiembrosRequest.getEmail())).thenReturn(Optional.of(usuarioInvitado));
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuarioInvitado.getId(), grupo.getId())).thenReturn(Optional.of(miembroGrupo));

        assertThrows(UsuarioPerteneceAlGrupoException.class, () -> invitacionServiceImpl.invitarUsuarios(grupo.getId(), invitarMiembrosRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiElInvitadoYaTieneUnaInvitacionPendiente(){

        doNothing().when(groupPermissionService).validarAdministrador(grupo.getId());
        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(usuarioRepository.findByEmail(invitarMiembrosRequest.getEmail())).thenReturn(Optional.of(usuarioInvitado));
        when(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuarioInvitado.getId(), grupo.getId())).thenReturn(Optional.empty());
        when(invitacionRepository.existsByGrupo_IdAndInvitado_IdAndEstado(grupo.getId(), usuarioInvitado.getId(), EstadoInvitacion.PENDIENTE)).thenReturn(true);
    
        assertThrows(InvitacionPendienteException.class, () -> invitacionServiceImpl.invitarUsuarios(grupo.getId(), invitarMiembrosRequest));
    }

    @Test
    void deberiaLnzarExcepcionSiInvitacionNoEncontrada(){

        usuario.setId(usuarioInvitado.getId());

        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(invitacionRepository.findById(invitacion.getId())).thenReturn(Optional.empty());

        // Este test funciona para rechazarInvitacion, solo hay que cambiar esto invitacionServiceImpl.aceptarInvitacion por invitacionServiceImpl.rechazarInvitacion
        assertThrows(InvitacionNoEncontrada.class, () -> invitacionServiceImpl.aceptarInvitacion(invitacion.getId()));
    }

    @Test
    void deberiaLanzarExcepcionSiIntentasAceptarUnaInvitacionQueNoEsTuya(){

        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(invitacionRepository.findById(invitacion.getId())).thenReturn(Optional.of(invitacion));

        // Este test funciona para rechazarInvitacion, solo hay que cambiar esto invitacionServiceImpl.aceptarInvitacion por invitacionServiceImpl.rechazarInvitacion
        assertThrows(NoEsTuInvitacionException.class, () -> invitacionServiceImpl.aceptarInvitacion(invitacion.getId()));

    }

    @Test
    void deberiaLanzarExcepcionSiLaInvitacionYaFueRespondida(){

        usuario.setId(usuarioInvitado.getId());
        invitacion.setEstado(EstadoInvitacion.ACEPTADA);

        when(currentUserService.obtenerUsuarioActual()).thenReturn(usuario);
        when(invitacionRepository.findById(invitacion.getId())).thenReturn(Optional.of(invitacion));

        // Este test funciona para rechazarInvitacion, solo hay que cambiar esto invitacionServiceImpl.aceptarInvitacion por invitacionServiceImpl.rechazarInvitacion
        assertThrows(InvitacionRespondidaException.class, () -> invitacionServiceImpl.aceptarInvitacion(invitacion.getId()));
    }

}
