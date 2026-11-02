package com.aldo.deuda_cero.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.deuda_cero.entity.RefreshToken;
import com.aldo.deuda_cero.entity.Usuario;

public interface RefreshTokenResporitory extends JpaRepository<RefreshToken, Long>{

    Optional<RefreshToken> findByToken(String refreshToken);
    void deleteByToken(String refreshToken);

    @Transactional
    void deleteByUsuario(Usuario usuario);

    @Transactional
    void deleteByUsuario_Email(String email);
}
