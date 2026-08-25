package com.aldo.deuda_cero.exception;

public class EmailExistenteException extends RuntimeException{

    public EmailExistenteException(){
        super("Este email ya esta en uso. Intenta nuevamente");
    }
}
