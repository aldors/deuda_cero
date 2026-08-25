package com.aldo.deuda_cero.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovimientoRecienteResponse {
    
    private Long movimientoId;
    private String descripcion;
    private String pagador;
    private BigDecimal montoTotal;
    private LocalDateTime fechaMovimiento;
}
