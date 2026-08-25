package com.aldo.deuda_cero.dto.deuda;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeudaBrutaResponse {
    private Long deudorId;
    private String deudor;
    private Long acreedorId;
    private String acreedor;
    private BigDecimal monto;
}
