package com.aldo.deuda_cero.dto.invitaciones;

import com.aldo.deuda_cero.entity.enums.EstadoInvitacion;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvitacionResponse {
    
    private Long id;
    private String grupo;
    private String invitado;
    private EstadoInvitacion estado;
}
