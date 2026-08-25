package com.aldo.deuda_cero.exception;

public class NoEsTuInvitacionException extends RuntimeException{
    
    public NoEsTuInvitacionException(){
        super("Lo sentimos, no puedes aceptar una invitacion que no te pertenece");
    }
}
