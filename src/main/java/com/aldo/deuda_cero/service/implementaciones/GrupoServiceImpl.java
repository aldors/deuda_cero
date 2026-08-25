package com.aldo.deuda_cero.service.implementaciones;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aldo.deuda_cero.dto.grupo.GrupoRequest;
import com.aldo.deuda_cero.dto.grupo.GrupoResponse;
import com.aldo.deuda_cero.dto.grupo.MiembrosResponse;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;
import com.aldo.deuda_cero.exception.GrupoNoEncontradoException;
import com.aldo.deuda_cero.mapper.GrupoMapper;
import com.aldo.deuda_cero.repository.GrupoRepository;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.security.CurrentUserService;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.interfaces.GrupoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GrupoServiceImpl implements GrupoService{

    private final CurrentUserService currentUserService;
    private final GroupPermissionService groupPermissionService;
    private final GrupoRepository grupoRepository;
    private final MiembroGrupoRepository miembroGrupoRepository;

    @Override
    public GrupoResponse crearGrupo(GrupoRequest grupoRequest) {

        Usuario usuario = currentUserService.obtenerUsuarioActual();

        Grupo grupo = GrupoMapper.toEntity(grupoRequest);

        grupo =  grupoRepository.save(grupo);

        MiembroGrupo miembroGrupo = MiembroGrupo.builder()
            .usuario(usuario)
            .grupo(grupo)
            .rol(RolGrupo.AMDIN)
            .estado(EstadoMiembro.ACTIVO)
            .fechaIngreso(LocalDateTime.now())
            .build();

        miembroGrupoRepository.save(miembroGrupo);

        return GrupoMapper.toResponse(grupo, 1L);
    }

    @Override
    public List<GrupoResponse> obtenerMisGrupos() {

        Usuario usuario = currentUserService.obtenerUsuarioActual();
        return miembroGrupoRepository.obtenerGruposDelUsuario(usuario.getId());
    }

    @Override
    public List<MiembrosResponse> obtenerMiembros(Long grupoId) {
        
        if(!grupoRepository.existsById(grupoId)){
            throw new GrupoNoEncontradoException();
        }

        groupPermissionService.obtenerMiembroActual(grupoId);

        List<MiembroGrupo> miembros = miembroGrupoRepository.findByGrupoIdAndEstado(grupoId, EstadoMiembro.ACTIVO);

        return miembros.stream()
            .map(mg -> new MiembrosResponse(
                    mg.getUsuario().getId(),
                    mg.getUsuario().getNombre(),
                    mg.getRol().toString()))
            .toList();
    }
    
}
