package com.aldo.deuda_cero.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aldo.deuda_cero.dto.balance.TotalConsumidoResponse;
import com.aldo.deuda_cero.entity.ParticipacionMovimiento;

public interface ParticipacionMovimientoRepository extends JpaRepository<ParticipacionMovimiento, Long>{
    
    @Query("""
    SELECT new com.aldo.deuda_cero.dto.balance.TotalConsumidoResponse(
        pm.miembroGrupo.id,
        SUM(pm.montoCorrespondiente)
    )
    FROM ParticipacionMovimiento pm
    WHERE pm.movimiento.grupo.id = :grupoId
    GROUP BY pm.miembroGrupo.id
    """)
    List<TotalConsumidoResponse> obtenerTotalConsumidoPorMiembro(@Param("grupoId") Long grupoId);
}
