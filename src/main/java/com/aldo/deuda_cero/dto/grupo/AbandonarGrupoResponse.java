package com.aldo.deuda_cero.dto.grupo;

import com.aldo.deuda_cero.entity.enums.EstadoMiembro;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AbandonarGrupoResponse {

    private Long miembroId;
    private String nombreMiembro;
    private Long grupoId;
    private String nombreGrupo;
    private EstadoMiembro estadoMiembro;
    
}
