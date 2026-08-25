package com.aldo.deuda_cero.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aldo.deuda_cero.entity.PagoDeuda;

public interface PagoDeudaRepository extends JpaRepository<PagoDeuda, Long>{
    
    //List<PagoDeuda> findByGrupoIdOrderByFechaDesc(Long grupoId);

    /*
    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM PagoDeuda p
        WHERE p.grupo.id = :grupoId
        AND p.pagador.id = :pagadorId
        AND p.receptor.id = :receptorId
    """)
    BigDecimal obtenerTotalPagadoEntreMiembros(
            @Param("grupoId") Long grupoId,
            @Param("pagadorId") Long pagadorId,
            @Param("receptorId") Long receptorId
    );
    */

    //List<PagoDeuda> findByGrupoId(Long grupoId);


    /*Este metodo esta modificado para que busque el grupo, pagador y receptor dentro de la
    entidad Deuda, y que no busque directamente el grupo en la entidad PagoDeuda ya que ahi no hay
    relación*/
    List<PagoDeuda> findByDeudaGrupoIdOrderByFechaPagoDesc(Long grupoId);
}
