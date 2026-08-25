package com.aldo.deuda_cero.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aldo.deuda_cero.entity.Grupo;

public interface GrupoRepository extends JpaRepository<Grupo, Long>{
    
}
