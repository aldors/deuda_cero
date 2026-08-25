package com.aldo.deuda_cero.dto.pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class PagoResponse {

    private Long id;

    private String pagador;

    private String receptor;

    private BigDecimal monto;

    private LocalDateTime fechaPago;
}
