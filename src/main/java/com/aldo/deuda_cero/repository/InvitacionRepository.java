package com.aldo.deuda_cero.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aldo.deuda_cero.dto.invitaciones.InvitacionesPendientesResponse;
import com.aldo.deuda_cero.entity.Invitacion;

public interface InvitacionRepository extends JpaRepository<Invitacion,Long> {

    Optional<Invitacion> findByGrupoIdAndInvitadoId(
            Long grupoId,
            Long invitadoId
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
