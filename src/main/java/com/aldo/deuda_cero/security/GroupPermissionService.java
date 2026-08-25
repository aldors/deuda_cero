package com.aldo.deuda_cero.security;

import org.springframework.stereotype.Service;

import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.exception.NoPermisosException;
import com.aldo.deuda_cero.exception.NoPerteneceAlGrupoException;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupPermissionService {
    
    private final CurrentUserService currentUserService;
    private final MiembroGrupoRepository miembroGrupoRepository;

    public MiembroGrupo obtenerMiembroActual(Long grupoId) {

        Usuario usuario = currentUserService.obtenerUsuarioActual();

        return miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario.getId(), grupoId)
                .orElseThrow(() -> new NoPerteneceAlGrupoException());
    }

    //Es lo mismo que la de arriba pero para las invitaciones se hace para no llamar 2 veces a currentUserService.obtenerUsuarioActual();
    public MiembroGrupo obtenerMiembro(Long grupoId, Usuario usuario) {

        return miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario.getId(), grupoId)
                .orElseThrow(() -> new NoPerteneceAlGrupoException());
    }


    
    public void validarAdministrador(Long grupoId) {

        MiembroGrupo miembro = obtenerMiembroActual(grupoId);

        if (miembro.getRol() != RolGrupo.AMDIN) {
            throw new NoPermisosException();
        }

    }

    //Este lo hice ya que validarAdministrador() y obtenerMiembroActual() se usan juntos, entonces este lo hace en uno solo
    public MiembroGrupo obtenerAdministradorActual(Long grupoId) {

        Usuario usuario = currentUserService.obtenerUsuarioActual();

        MiembroGrupo miembro = miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuario.getId(), grupoId)
                .orElseThrow(() -> new NoPerteneceAlGrupoException());

        if (miembro.getRol() != RolGrupo.AMDIN) {
            throw new NoPermisosException();
        }
        
        return miembro;

    }
}