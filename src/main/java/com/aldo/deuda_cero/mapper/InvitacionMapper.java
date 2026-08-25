package com.aldo.deuda_cero.mapper;

import com.aldo.deuda_cero.dto.invitaciones.InvitacionResponse;
import com.aldo.deuda_cero.entity.Invitacion;

public class InvitacionMapper {
    
    public static InvitacionResponse toResponse(Invitacion invitacion){

        return new InvitacionResponse(
            invitacion.getId(),
            invitacion.getGrupo().getNombre(),
            invitacion.getInvitado().getNombre(),
            invitacion.getEstado()
        );
    }
}
