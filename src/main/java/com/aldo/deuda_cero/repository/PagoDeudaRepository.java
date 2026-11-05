package com.aldo.deuda_cero.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aldo.deuda_cero.dto.balance.TotalConsumidoResponse;
import com.aldo.deuda_cero.dto.balance.TotalPagadoResponse;
import com.aldo.deuda_cero.entity.PagoDeuda;

public interface PagoDeudaRepository extends JpaRepository<PagoDeuda, Long>{

    List<PagoDeuda> findByDeudaGrupoIdOrderByFechaPagoDesc(Long grupoId);

    @Query("""
    SELECT new com.aldo.deuda_cero.dto.balance.TotalPagadoResponse(
        d.deudor.id,
        SUM(p.monto)
    )
    FROM PagoDeuda p
    JOIN p.deuda d
    WHERE d.grupo.id = :grupoId
    GROUP BY d.deudor.id
    """)
    List<TotalPagadoResponse> obtenerTotalPagadoEnDeudas(@Param("grupoId") Long grupoId);

    @Query("""
    SELECT new com.aldo.deuda_cero.dto.balance.TotalConsumidoResponse(
        d.acreedor.id,
        SUM(p.monto)
    )
    FROM PagoDeuda p
    JOIN p.deuda d
    WHERE d.grupo.id = :grupoId
    GROUP BY d.acreedor.id
    """)
    List<TotalConsumidoResponse> obtenerTotalRecibidoEnDeudas(@Param("grupoId") Long grupoId);
}
