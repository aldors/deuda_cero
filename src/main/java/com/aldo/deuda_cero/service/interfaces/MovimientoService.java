package com.aldo.deuda_cero.service.interfaces;

import java.util.List;

import com.aldo.deuda_cero.dto.Movimientos.CrearMovimientoRequest;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoDetalleResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResumenResponse;

public interface MovimientoService {
    
    public MovimientoResponse crearMovimiento(Long grupoId, CrearMovimientoRequest crearMovimientoRequest);
    
    List<MovimientoResumenResponse> obtenerMovimientos(Long grupoId);
    MovimientoDetalleResponse obtenerDetalleMovimiento(Long grupoId, Long movimientoId);

}
