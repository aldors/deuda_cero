package com.aldo.deuda_cero.exception;

public class DeudaNoEncontradaException extends RuntimeException{
    
    public DeudaNoEncontradaException(){
        super("La deuda no ha sido encontrada");
    }
}
