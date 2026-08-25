package com.aldo.deuda_cero.exception;

public class InvitacionNoEncontrada extends RuntimeException{
    
    public InvitacionNoEncontrada(){
        super("La invitacion no ha sido encontrada");
    }
}
