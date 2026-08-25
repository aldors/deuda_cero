package com.aldo.deuda_cero.dto.deuda;

import java.math.BigDecimal;

import com.aldo.deuda_cero.entity.enums.EstadoDeuda;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeudaResponse {
    
    /* esto no se si se seguira usando
    private Long deudorId;
    private String deudor;
    private Long acreedorId;
    private String acreedor;
    private BigDecimal monto;
    */

    private Long id;
    private Long deudorId;
    private String deudor;
    private Long acreedorId;
    private String acreedor;
    private BigDecimal montoOriginal;
    private BigDecimal montoPendiente;
    private EstadoDeuda estado;
}
