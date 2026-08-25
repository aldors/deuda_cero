package com.aldo.deuda_cero.dto.grupo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GrupoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Long totalMiembros;
}
