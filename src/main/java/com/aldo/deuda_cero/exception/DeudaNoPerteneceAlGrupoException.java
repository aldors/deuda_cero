package com.aldo.deuda_cero.exception;

public class DeudaNoPerteneceAlGrupoException extends RuntimeException{
    
    public DeudaNoPerteneceAlGrupoException(){
        super("Esta deuda no pertenece a este grupo");
    }
}
