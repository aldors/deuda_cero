package com.aldo.deuda_cero.exception;

public class MovimientoNoEncontradoException extends RuntimeException{
    
    public MovimientoNoEncontradoException(){
        super("El movimiento no ha sido encontrado");
    }
}
