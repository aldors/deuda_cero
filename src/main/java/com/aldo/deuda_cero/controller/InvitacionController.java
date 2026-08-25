package com.aldo.deuda_cero.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.deuda_cero.dto.invitaciones.AceptarInvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionesPendientesResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitarMiembrosRequest;
import com.aldo.deuda_cero.service.interfaces.InvitacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/invitaciones")
@RequiredArgsConstructor
public class InvitacionController {

    private final InvitacionService invitacionService;
    
    @PostMapping("/invitar/{grupoId}")
    public ResponseEntity<InvitacionResponse> invitarUsuarios(@PathVariable Long grupoId, @Valid @RequestBody InvitarMiembrosRequest invitarMiembrosRequest){
        return ResponseEntity.ok(invitacionService.invitarUsuarios(grupoId, invitarMiembrosRequest));
    }

    @GetMapping("/invitaciones")
    public ResponseEntity<List<InvitacionesPendientesResponse>> obtenerInvitaciones(){
        return ResponseEntity.ok(invitacionService.obtenerInvitaciones());
    }

    @PostMapping("/invitaciones/{invitacionId}/aceptar")
    public ResponseEntity<AceptarInvitacionResponse> aceptarInvitacion(@PathVariable Long invitacionId){
        return ResponseEntity.ok(invitacionService.aceptarInvitacion(invitacionId));
    }

    @PostMapping("/invitaciones/{invitacionId}/rechazar")
    public ResponseEntity<AceptarInvitacionResponse> rechazarInvitacion(@PathVariable Long invitacionId){
        return ResponseEntity.ok(invitacionService.rechazarInvitacion(invitacionId));
    }
}
