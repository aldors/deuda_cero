package com.aldo.deuda_cero.dto.deuda;

import java.math.BigDecimal;

import com.aldo.deuda_cero.entity.MiembroGrupo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImpactoDeudaResponse {

    private MiembroGrupo deudor;
    private MiembroGrupo acreedor;
    private BigDecimal monto;
}
