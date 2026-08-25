package com.aldo.deuda_cero.dto.Movimientos;

import java.math.BigDecimal;

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
public class ParticipacionResponse {

    private Long usuarioId;
    private String nombre;
    private BigDecimal monto;
}
