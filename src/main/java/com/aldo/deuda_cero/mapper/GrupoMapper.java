package com.aldo.deuda_cero.mapper;

import org.springframework.stereotype.Component;

import com.aldo.deuda_cero.dto.grupo.GrupoRequest;
import com.aldo.deuda_cero.dto.grupo.GrupoResponse;
import com.aldo.deuda_cero.entity.Grupo;

@Component
public class GrupoMapper {
    
    public static Grupo toEntity(GrupoRequest grupoRequest){

        return Grupo.builder()
                .nombre(grupoRequest.getNombre())
                .descripcion(grupoRequest.getDescripcion())
                .build();
    }

    public static GrupoResponse toResponse(Grupo grupo, Long totalMiembros){

        return new GrupoResponse(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getDescripcion(),
                totalMiembros
        );
    }
}
