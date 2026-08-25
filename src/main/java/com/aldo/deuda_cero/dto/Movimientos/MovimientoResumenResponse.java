package com.aldo.deuda_cero.dto.Movimientos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aldo.deuda_cero.entity.enums.TipoDivision;
import com.aldo.deuda_cero.entity.enums.TipoMovimiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoResumenResponse {
    
    private Long id;

    private String descripcion;

    private BigDecimal montoTotal;

    private String pagador;

    private String registradoPor;

    private TipoMovimiento tipo;

    private TipoDivision tipoDivision;

    private LocalDateTime fechaMovimiento;
}
