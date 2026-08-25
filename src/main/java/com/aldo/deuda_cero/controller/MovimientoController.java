package com.aldo.deuda_cero.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.deuda_cero.dto.Movimientos.CrearMovimientoRequest;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoDetalleResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResumenResponse;
import com.aldo.deuda_cero.service.interfaces.MovimientoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;
    
    @PostMapping("/crear/{grupoId}")
    public ResponseEntity<MovimientoResponse> crearMovimiento(@PathVariable Long grupoId, @Valid @RequestBody CrearMovimientoRequest crearMovimientoRequest){
        return ResponseEntity.ok(movimientoService.crearMovimiento(grupoId, crearMovimientoRequest));
    }

    //consultar movimientos del grupo: GET /grupos/{id}/movimientos
    @GetMapping("/{grupoId}/movimientos")
    public ResponseEntity<List<MovimientoResumenResponse>> obtenerMovimientos(@PathVariable Long grupoId){
        return ResponseEntity.ok(movimientoService.obtenerMovimientos(grupoId));
    }

    //consultar detalles de un movimiento: GET /movimientos/{id}
    @GetMapping("/grupos/{grupoId}/movimientos/{movimientoId}")
    public ResponseEntity<MovimientoDetalleResponse> obtenerDetalle(@PathVariable Long grupoId, @PathVariable Long movimientoId){
        return ResponseEntity.ok(movimientoService.obtenerDetalleMovimiento(grupoId, movimientoId)
        );
    }
}

/*

Movimiento 1:

cecilia paga 200 pesos de un taxi.
participa: ella, isaac y aldo.
division de dinero: equitativa
¿Quien debe?: isaac debe 66.66666667 a cecilia, aldo debe 66.66666667 a cecilia

*/
