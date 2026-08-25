package com.aldo.deuda_cero.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aldo.deuda_cero.entity.Deuda;
import com.aldo.deuda_cero.entity.enums.EstadoDeuda;

public interface DeudaRepository extends JpaRepository<Deuda, Long>{
    
    List<Deuda> findByGrupoId(Long grupoId);
    List<Deuda> findByGrupoIdAndEstado(Long grupoId, EstadoDeuda estado);
    Optional<Deuda> findByGrupoIdAndDeudorIdAndAcreedorId(Long grupoId, Long deudorId, Long acreedorId);
    Optional<Deuda> findByGrupoIdAndDeudorIdAndAcreedorIdAndEstado(Long grupoId, Long deudorId, Long acreedorId, EstadoDeuda estado);

    List<Deuda> findByGrupoIdAndDeudorIdAndEstado(
            Long grupoId,
            Long deudorId,
            EstadoDeuda estado
    );

    List<Deuda> findByGrupoIdAndAcreedorIdAndEstado(
            Long grupoId,
            Long acreedorId,
            EstadoDeuda estado
    );

}
