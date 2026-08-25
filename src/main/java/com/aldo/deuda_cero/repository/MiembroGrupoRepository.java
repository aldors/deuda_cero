package com.aldo.deuda_cero.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aldo.deuda_cero.dto.grupo.GrupoResponse;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;

public interface MiembroGrupoRepository extends JpaRepository<MiembroGrupo, Long>{

    Optional<MiembroGrupo> findByUsuarioIdAndGrupoId(
            Long usuarioId,
            Long grupoId
    );

    List<MiembroGrupo> findByGrupoIdAndUsuarioIdIn(
            Long grupoId,
            List<Long> usuarioIds
    );

    @Query("""
    SELECT new com.aldo.deuda_cero.dto.grupo.GrupoResponse(
        g.id,
        g.nombre,
        g.descripcion,
        (
            SELECT COUNT(mg2)
            FROM MiembroGrupo mg2
            WHERE mg2.grupo.id = g.id
        )
    )
    FROM MiembroGrupo mg
    JOIN mg.grupo g
    WHERE mg.usuario.id = :usuarioId
    AND mg.estado = com.aldo.deuda_cero.entity.enums.EstadoMiembro.ACTIVO
    """)
    List<GrupoResponse> obtenerGruposDelUsuario(Long usuarioId);

    List<MiembroGrupo> findByGrupoIdAndEstado(Long grupoId, EstadoMiembro estado);

}
