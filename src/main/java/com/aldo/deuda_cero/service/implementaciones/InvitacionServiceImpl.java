package com.aldo.deuda_cero.service.implementaciones;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.deuda_cero.dto.invitaciones.AceptarInvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionesPendientesResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitarMiembrosRequest;
import com.aldo.deuda_cero.entity.Invitacion;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoInvitacion;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.exception.InvitacionATiMismoException;
import com.aldo.deuda_cero.exception.InvitacionNoEncontrada;
import com.aldo.deuda_cero.exception.InvitacionPendienteException;
import com.aldo.deuda_cero.exception.InvitacionRespondidaException;
import com.aldo.deuda_cero.exception.NoEsTuInvitacionException;
import com.aldo.deuda_cero.exception.UsuarioNoEncontradoException;
import com.aldo.deuda_cero.exception.UsuarioPerteneceAlGrupoException;
import com.aldo.deuda_cero.mapper.InvitacionMapper;
import com.aldo.deuda_cero.repository.InvitacionRepository;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.repository.UsuarioRepository;
import com.aldo.deuda_cero.security.CurrentUserService;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.interfaces.InvitacionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvitacionServiceImpl implements InvitacionService{

    private final CurrentUserService currentUserService;
    private final MiembroGrupoRepository miembroGrupoRepository;
    private final GroupPermissionService groupPermissionService;
    private final UsuarioRepository usuarioRepository;
    private final InvitacionRepository invitacionRepository;

    @Override
    public InvitacionResponse invitarUsuarios(Long grupoId, InvitarMiembrosRequest invitarMiembrosRequest) {
        
        //MiembroGrupo administrador = groupPermissionService.obtenerAdministradorActual(grupoId);
        
        ///
        groupPermissionService.validarAdministrador(grupoId);

        ///
        Usuario usuarioActual = currentUserService.obtenerUsuarioActual();

        Usuario usuarioInvitado = usuarioRepository.findByEmail(invitarMiembrosRequest.getEmail())
            .orElseThrow(() -> new UsuarioNoEncontradoException());

        if(usuarioActual.getId().equals(usuarioInvitado.getId())){
            throw new InvitacionATiMismoException();
        }

        if(miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuarioInvitado.getId(), grupoId).isPresent()){
            throw new UsuarioPerteneceAlGrupoException();
        }

        if(invitacionRepository.existsByGrupo_IdAndInvitado_IdAndEstado(grupoId, usuarioInvitado.getId(), EstadoInvitacion.PENDIENTE)){
            throw new InvitacionPendienteException();
        }

        ///
        MiembroGrupo administrador = groupPermissionService.obtenerMiembro(grupoId, usuarioActual);

        Invitacion invitacion = Invitacion.builder()
            .grupo(administrador.getGrupo())
            .invitado(usuarioInvitado)
            .invitador(usuarioActual)
            .estado(EstadoInvitacion.PENDIENTE)
            .fechaEnvio(LocalDateTime.now())
            .build();

        invitacion = invitacionRepository.save(invitacion);

        return InvitacionMapper.toResponse(invitacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitacionesPendientesResponse> obtenerInvitaciones() {
        
        Long usuarioId = currentUserService.obtenerIdUsuarioActual();

        return invitacionRepository.obtenerInvitacionesPendientes(usuarioId);
    }

    @Override
    @Transactional
    public AceptarInvitacionResponse aceptarInvitacion(Long invitacionId) {

        Usuario usuarioActual = currentUserService.obtenerUsuarioActual();

        Invitacion invitacion = invitacionRepository.findById(invitacionId)
                .orElseThrow(() ->
                        new InvitacionNoEncontrada());

        if (!invitacion.getInvitado().getId().equals(usuarioActual.getId())) {
            throw new NoEsTuInvitacionException();
        }

        if (invitacion.getEstado() != EstadoInvitacion.PENDIENTE) {
            throw new InvitacionRespondidaException();
        }

        invitacion.setEstado(EstadoInvitacion.ACEPTADA);
        
        invitacionRepository.save(invitacion);
        
        MiembroGrupo miembro = MiembroGrupo.builder()
                .usuario(usuarioActual)
                .grupo(invitacion.getGrupo())
                .rol(RolGrupo.MIEMBRO)
                .estado(EstadoMiembro.ACTIVO)
                .fechaIngreso(LocalDateTime.now())
                .build();

        miembroGrupoRepository.save(miembro);

        return new AceptarInvitacionResponse(
                invitacion.getGrupo().getId(),
                invitacion.getGrupo().getNombre(),
                "Te uniste correctamente al grupo"
        );
    }

    @Override
    @Transactional
    public AceptarInvitacionResponse rechazarInvitacion(Long invitacionId) {
        
        Usuario usuarioActual = currentUserService.obtenerUsuarioActual();

        Invitacion invitacion = invitacionRepository.findById(invitacionId)
                .orElseThrow(() ->
                        new InvitacionNoEncontrada());

        if (!invitacion.getInvitado().getId().equals(usuarioActual.getId())) {
            throw new NoEsTuInvitacionException();
        }

        if (invitacion.getEstado() != EstadoInvitacion.PENDIENTE) {
            throw new InvitacionRespondidaException();
        }

        invitacion.setEstado(EstadoInvitacion.RECHAZADA);
        
        invitacionRepository.save(invitacion);

        return new AceptarInvitacionResponse(
                invitacion.getGrupo().getId(),
                invitacion.getGrupo().getNombre(),
                "La invitacion fue rechazada correctamente"
        );
    }

}
