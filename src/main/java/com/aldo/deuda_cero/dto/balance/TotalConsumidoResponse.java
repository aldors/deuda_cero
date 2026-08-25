package com.aldo.deuda_cero.dto.balance;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TotalConsumidoResponse {

    private Long miembroGrupoId;
    private BigDecimal gtotalConsumido;
}
