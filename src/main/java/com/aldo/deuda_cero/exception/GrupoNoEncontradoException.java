package com.aldo.deuda_cero.exception;

public class GrupoNoEncontradoException extends RuntimeException{
    
    public GrupoNoEncontradoException(){
        super("El grupo no ha sido encontrado");
    }
}
