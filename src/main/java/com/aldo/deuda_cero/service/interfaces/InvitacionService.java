package com.aldo.deuda_cero.service.interfaces;

import java.util.List;

import com.aldo.deuda_cero.dto.invitaciones.AceptarInvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitacionesPendientesResponse;
import com.aldo.deuda_cero.dto.invitaciones.InvitarMiembrosRequest;

public interface InvitacionService {
    
    public InvitacionResponse invitarUsuarios(Long grupoId, InvitarMiembrosRequest invitarMiembrosRequest);
    public List<InvitacionesPendientesResponse> obtenerInvitaciones();
    public AceptarInvitacionResponse aceptarInvitacion(Long invitacionId);
    public AceptarInvitacionResponse rechazarInvitacion(Long invitacionId);
}
