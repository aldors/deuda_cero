package com.aldo.deuda_cero.exception;

public class NoPermisosException extends RuntimeException{
    
    public NoPermisosException(){
        super("No tienes permisos para realizar esta acción");
    }
}
