package com.aldo.deuda_cero.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aldo.deuda_cero.entity.RefreshToken;

public interface RefreshTokenResporitory extends JpaRepository<RefreshToken, Long>{

    Optional<RefreshToken> findByToken(String refreshToken);
    void deleteByToken(String refreshToken);
}
