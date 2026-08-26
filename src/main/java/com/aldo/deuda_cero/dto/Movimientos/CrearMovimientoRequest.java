package com.aldo.deuda_cero.dto.Movimientos;

import java.math.BigDecimal;
import java.util.List;

import com.aldo.deuda_cero.entity.enums.TipoDivision;
import com.aldo.deuda_cero.entity.enums.TipoMovimiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearMovimientoRequest {
    
    @NotBlank(message = "La descripción del movimiento es obligatoria")
    @Size(max = 255)
    private String descripcion;

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    private BigDecimal montoTotal;

    @NotNull(message = "Debe indicar quien realizó el pago")
    private Long pagadorId;

    @NotNull(message = "Debe indicar el tipo de movimiento")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "Debe indicar el tipo de division")
    private TipoDivision tipoDivision;

    @NotEmpty(message = "Debe haber al menos un participante")
    private List<ParticipanteMovimientoRequest> participantes;
}
