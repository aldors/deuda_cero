package com.aldo.deuda_cero.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.entity.enums.RolGrupo;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "miembros_grupo",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "grupo_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiembroGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolGrupo rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMiembro estado;

    private LocalDateTime fechaIngreso;

    @OneToMany(mappedBy = "pagador")
    private List<Movimiento> movimientosPagados;

    @OneToMany(mappedBy = "registradoPor")
    private List<Movimiento> movimientosRegistrados;

    @OneToMany(mappedBy = "miembroGrupo")
    private List<ParticipacionMovimiento> participaciones;
}
