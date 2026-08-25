package com.aldo.deuda_cero.dto.deuda;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SaldoPendienteResponse {
    
    private Long miembroGrupoId; //Cambie de usuarioId a miembroGrupoId
    private String nombre;
    private BigDecimal monto;
}
