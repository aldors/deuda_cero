package com.aldo.deuda_cero.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.aldo.deuda_cero.dto.pagos.RegistrarPagoRequest;
import com.aldo.deuda_cero.dto.pagos.PagoResponse;
import com.aldo.deuda_cero.entity.Deuda;
import com.aldo.deuda_cero.entity.PagoDeuda;

@Component
public class PagoDeudaMapper {
    
    public static PagoDeuda toEntity(RegistrarPagoRequest registrarPagoRequest, Deuda deuda){

        return PagoDeuda.builder()
                .deuda(deuda)
                .monto(registrarPagoRequest.getMonto())
                .fechaPago(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    public static PagoResponse toResponse(PagoDeuda pagoDeuda){

        return PagoResponse.builder()
            .id(pagoDeuda.getId())
            .pagador(pagoDeuda.getDeuda().getDeudor().getUsuario().getNombre())
            .receptor(pagoDeuda.getDeuda().getAcreedor().getUsuario().getNombre())
            .monto(pagoDeuda.getMonto())
            .fechaPago(pagoDeuda.getFechaPago())
            .build();
    }
}
