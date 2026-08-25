package com.aldo.deuda_cero.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aldo.deuda_cero.entity.enums.EstadoDeuda;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "deudas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deudor_id", nullable = false)
    private MiembroGrupo deudor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acreedor_id", nullable = false)
    private MiembroGrupo acreedor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoOriginal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPendiente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDeuda estado;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;
}