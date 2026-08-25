package com.aldo.deuda_cero.dto.dashboard;

import java.math.BigDecimal;

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
public class DeudaResumenResponse {
    
    private Long deudaId;
    private Long usuarioId;
    private String nombre;
    private BigDecimal montoPendiente;
}
