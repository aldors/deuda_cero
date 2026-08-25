package com.aldo.deuda_cero.dto.grupo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ObtenerUnGrupoResponse {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaCreacion;
}
