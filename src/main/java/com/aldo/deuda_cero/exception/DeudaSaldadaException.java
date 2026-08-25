package com.aldo.deuda_cero.exception;

public class DeudaSaldadaException extends RuntimeException{
    
    public DeudaSaldadaException(){
        super("Esta deuda ya ha sido saldada");
    }
}
