package com.aldo.deuda_cero.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aldo.deuda_cero.dto.invitaciones.InvitacionesPendientesResponse;
import com.aldo.deuda_cero.entity.Invitacion;
import com.aldo.deuda_cero.entity.enums.EstadoInvitacion;

public interface InvitacionRepository extends JpaRepository<Invitacion,Long> {

    boolean existsByGrupo_IdAndInvitado_IdAndEstado(
            Long grupoId,
            Long invitadoId,
            EstadoInvitacion estado
    );

    @Query("""
    SELECT new com.aldo.deuda_cero.dto.invitaciones.InvitacionesPendientesResponse(
        i.id,
        i.grupo.nombre,
        CONCAT(i.invitador.nombre, ' ',i.invitador.apellido),
        i.estado,
        i.fechaEnvio
    )
    FROM Invitacion i
    WHERE i.invitado.id = :usuarioId
    AND i.estado = com.aldo.deuda_cero.entity.enums.EstadoInvitacion.PENDIENTE
    ORDER BY i.fechaEnvio DESC
    """)
    List<InvitacionesPendientesResponse> obtenerInvitacionesPendientes(Long usuarioId);

}
