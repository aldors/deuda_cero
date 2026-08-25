package com.aldo.deuda_cero.entity;

import java.time.LocalDateTime;

import com.aldo.deuda_cero.entity.enums.EstadoInvitacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "invitaciones",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"grupo_id", "invitado_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitado_id", nullable = false)
    private Usuario invitado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitador_id", nullable = false)
    private Usuario invitador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInvitacion estado;

    private LocalDateTime fechaEnvio;
}
