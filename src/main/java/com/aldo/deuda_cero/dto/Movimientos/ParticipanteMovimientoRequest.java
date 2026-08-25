package com.aldo.deuda_cero.dto.Movimientos;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class ParticipanteMovimientoRequest {
    
    @NotNull(message = "Debe indicar el miembro participante")
    Long usuarioId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    BigDecimal monto;
}
