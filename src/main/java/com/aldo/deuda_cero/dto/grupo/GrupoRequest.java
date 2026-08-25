package com.aldo.deuda_cero.dto.grupo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrupoRequest {
    
    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(min = 1, max = 50, message = "El tamaño debe estar entre 1 y 50")
    private String nombre;

    @Size(max = 255)
    private String descripcion;
}
