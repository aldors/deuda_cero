package com.aldo.deuda_cero.exception;

public class InvitacionRespondidaException extends RuntimeException{
    
    public InvitacionRespondidaException(){
        super("la invitación ya fue respondida por parte del invitado");
    }
}
