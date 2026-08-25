package com.aldo.deuda_cero.dto.pagos;

import java.math.BigDecimal;

//import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrarPagoRequest {
    
    @NotNull
    private Long deudaId; // Cambie de receptorId a deudaId

    @NotNull(message = "El monto el obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    private BigDecimal monto;
}
