package com.aldo.deuda_cero.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.aldo.deuda_cero.dto.grupo.GrupoRequest;
import com.aldo.deuda_cero.dto.grupo.GrupoResponse;
import com.aldo.deuda_cero.entity.Grupo;

// ¿Para que sirve anotarla como @Component y que es y como se usa MapStruct para clases mapper?
@Component
public class GrupoMapper {
    
    public static Grupo toEntity(GrupoRequest grupoRequest){

        return Grupo.builder()
                .nombre(grupoRequest.getNombre())
                .descripcion(grupoRequest.getDescripcion())
                .fechaCreacion(LocalDateTime.now())
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
