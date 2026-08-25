package com.aldo.deuda_cero.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aldo.deuda_cero.dto.balance.TotalPagadoResponse;
import com.aldo.deuda_cero.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long>{
    
    List<Movimiento> findByGrupoIdOrderByFechaMovimientoDesc(Long grupoId);

    @Query("""
    SELECT new com.aldo.deuda_cero.dto.balance.TotalPagadoResponse(
        m.pagador.id,
        SUM(m.montoTotal)
    )
    FROM Movimiento m
    WHERE m.grupo.id = :grupoId
    GROUP BY m.pagador.id
    """)
    List<TotalPagadoResponse> obtenerTotalPagadoPorMiembro(@Param("grupoId") Long grupoId);

    List<Movimiento> findTop10ByGrupoIdOrderByFechaMovimientoDesc(Long grupoId);
    Optional<Movimiento> findByIdAndGrupoId(Long movimientoId, Long grupoId);
}
