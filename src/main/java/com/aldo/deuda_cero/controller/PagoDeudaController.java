package com.aldo.deuda_cero.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.deuda_cero.dto.pagos.RegistrarPagoRequest;
import com.aldo.deuda_cero.dto.pagos.PagoResponse;
import com.aldo.deuda_cero.service.interfaces.PagoDeudaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoDeudaController {
    
    private final PagoDeudaService pagoDeudaService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{grupoId}/registrar")
    public ResponseEntity<Void> registrarPago(@PathVariable Long grupoId, @Valid @RequestBody RegistrarPagoRequest registrarPagoRequest){

        pagoDeudaService.registrarPago(grupoId, registrarPagoRequest);
        return ResponseEntity.ok().build();
    }

    //Consultar historial de pagos: GET /pagos/{grupoId}
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{grupoId}")
    public ResponseEntity<List<PagoResponse>> obtenerPagos(@PathVariable Long grupoId){
        return ResponseEntity.ok(pagoDeudaService.obtenerPagos(grupoId));
    }
}
