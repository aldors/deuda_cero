package com.aldo.deuda_cero.dto.invitaciones;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AceptarInvitacionResponse{

    private Long grupoId;
    private String grupo;
    private String mensaje;

}
