package com.aldo.deuda_cero.dto.invitaciones;

import java.time.LocalDateTime;

import com.aldo.deuda_cero.entity.enums.EstadoInvitacion;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvitacionesPendientesResponse {
    
    private Long id;
    private String grupo;
    private String invitador;
    private EstadoInvitacion estado;
    private LocalDateTime fechaEnvio;
}
