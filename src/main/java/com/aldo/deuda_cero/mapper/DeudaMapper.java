package com.aldo.deuda_cero.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aldo.deuda_cero.dto.deuda.DeudaResponse;
import com.aldo.deuda_cero.entity.Deuda;

@Component
public class DeudaMapper {
    
    public static DeudaResponse toResponse(Deuda deuda) {

        return new DeudaResponse(
                deuda.getId(),
                deuda.getDeudor().getId(),
                deuda.getDeudor().getUsuario().getNombre(),
                deuda.getAcreedor().getId(),
                deuda.getAcreedor().getUsuario().getNombre(),
                deuda.getMontoOriginal(),
                deuda.getMontoPendiente(),
                deuda.getEstado()
        );
    }

    public static List<DeudaResponse> toResponseList(List<Deuda> deudas) {

        return deudas.stream()
                .map(DeudaMapper::toResponse) //map(this::toResponse) *Cuando los metodos no son estaticos
                .toList();
    }
}
