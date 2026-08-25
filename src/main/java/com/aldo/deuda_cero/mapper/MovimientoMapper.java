package com.aldo.deuda_cero.mapper;

import org.springframework.stereotype.Component;

import com.aldo.deuda_cero.dto.Movimientos.CrearMovimientoRequest;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoDetalleResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResumenResponse;
import com.aldo.deuda_cero.dto.Movimientos.ParticipacionResponse;
import com.aldo.deuda_cero.entity.Movimiento;
import com.aldo.deuda_cero.entity.ParticipacionMovimiento;

@Component
public class MovimientoMapper {
    
    public static Movimiento toEntity(CrearMovimientoRequest crearMovimientoRequest){
        
        return Movimiento.builder()
            .descripcion(crearMovimientoRequest.getDescripcion())
            .montoTotal(crearMovimientoRequest.getMontoTotal())
            .tipo(crearMovimientoRequest.getTipoMovimiento())
            .build();
    }

    public static MovimientoResponse toResponse(Movimiento movimiento){

        return new MovimientoResponse(
            movimiento.getId(),
            movimiento.getDescripcion(),
            movimiento.getMontoTotal(),
            movimiento.getPagador().getUsuario().getNombre(),
            movimiento.getFechaMovimiento()
        );
    }

    public static MovimientoResumenResponse toResumenResponse(Movimiento movimiento){

        return MovimientoResumenResponse.builder()
            .id(movimiento.getId())
            .descripcion(movimiento.getDescripcion())
            .montoTotal(movimiento.getMontoTotal())
            .pagador(movimiento.getPagador().getUsuario().getNombre())
            .registradoPor(movimiento.getRegistradoPor().getUsuario().getNombre())
            .tipo(movimiento.getTipo())
            .tipoDivision(movimiento.getTipoDivision())
            .fechaMovimiento(movimiento.getFechaMovimiento())
            .build();
    }

    public static MovimientoDetalleResponse toDetalleResponse(Movimiento movimiento){

        return MovimientoDetalleResponse.builder()
            .id(movimiento.getId())
            .descripcion(movimiento.getDescripcion())
            .montoTotal(movimiento.getMontoTotal())
            .pagador(movimiento.getPagador().getUsuario().getNombre())
            .registradoPor(movimiento.getRegistradoPor().getUsuario().getNombre())
            .tipo(movimiento.getTipo())
            .tipoDivision(movimiento.getTipoDivision())
            .fechaMovimiento(movimiento.getFechaMovimiento())
            .participantes(movimiento.getParticipaciones().stream().map(MovimientoMapper::toParticipacionResponse).toList())
            .build();
    }

    public static ParticipacionResponse toParticipacionResponse(ParticipacionMovimiento participacion){

        return ParticipacionResponse.builder()

                .usuarioId(participacion.getMiembroGrupo().getUsuario().getId())
                .nombre(participacion.getMiembroGrupo().getUsuario().getNombre())
                .monto(participacion.getMontoCorrespondiente())
                .build();
    }
}
