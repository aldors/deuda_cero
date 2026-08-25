package com.aldo.deuda_cero.exception;

public class NoPerteneceAlGrupoException extends RuntimeException{
    
    public NoPerteneceAlGrupoException(){
        super("Lo sentimos, no perteneces a este grupo");
    }
}
