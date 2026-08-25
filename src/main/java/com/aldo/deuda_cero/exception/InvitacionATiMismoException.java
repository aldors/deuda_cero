package com.aldo.deuda_cero.exception;

public class InvitacionATiMismoException extends RuntimeException{
    
    public InvitacionATiMismoException(){
        super("No puedes invitarte tu mismo. Intenta invitar a otra persona");
    }
}
