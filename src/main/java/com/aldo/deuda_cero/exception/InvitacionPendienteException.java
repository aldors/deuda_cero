package com.aldo.deuda_cero.exception;

public class InvitacionPendienteException extends RuntimeException{
    
    public InvitacionPendienteException(){
        super("El usuario ya tiene una invitacion pendiente, espera su respuesta");
    }
}
