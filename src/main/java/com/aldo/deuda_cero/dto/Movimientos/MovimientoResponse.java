package com.aldo.deuda_cero.dto.Movimientos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovimientoResponse {
    
    private Long id;

    private String descripcion;

    private BigDecimal montoTotal;

    private String pagador;

    private LocalDateTime fechaMovimiento;
}
