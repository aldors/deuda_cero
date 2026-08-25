package com.aldo.deuda_cero.dto.balance;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BalanceMiembroResponse  {
    
    private Long miembroGrupoId; // cambie usuarioId por miembroGrupoId
    private String nombre;
    private BigDecimal totalPagado;
    private BigDecimal totalConsumido;
    private BigDecimal balance;
}
