package com.aldo.deuda_cero.dto.Movimientos;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipanteMovimientoRequest {
    
    @NotNull(message = "Debe indicar el miembro participante")
    Long usuarioId;

    @Digits(integer = 8, fraction = 2, message = "El monto debe tener máximo 2 decimales")
    BigDecimal monto;
}
