package com.aldo.deuda_cero.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.deuda_cero.dto.deuda.DeudaResponse;
import com.aldo.deuda_cero.service.interfaces.DeudaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
public class DeudaController {

    private final DeudaService deudaService;
    
    @GetMapping("/{grupoId}/deudas")
    public ResponseEntity<List<DeudaResponse>> obtenerDeudas(@PathVariable Long grupoId){
        return ResponseEntity.ok(deudaService.obtenerDeudas(grupoId));
    }

    @GetMapping("/{grupoId}/deudas/pendientes")
    public ResponseEntity<List<DeudaResponse>> obtenerDeudasPendientes(@PathVariable Long grupoId){
        return ResponseEntity.ok(deudaService.obtenerDeudasPendientes(grupoId));
    }

    @GetMapping("/{grupoId}/deudas/{deudaId}")
    public ResponseEntity<DeudaResponse> obtenerDeudasPorId(@PathVariable Long grupoId, @PathVariable Long deudaId){
        return ResponseEntity.ok(deudaService.obtenerDeudaPorId(grupoId, deudaId));
    }
}
 