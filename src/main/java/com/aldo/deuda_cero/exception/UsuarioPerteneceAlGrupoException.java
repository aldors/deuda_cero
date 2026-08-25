package com.aldo.deuda_cero.exception;

public class UsuarioPerteneceAlGrupoException extends RuntimeException{
    
    public UsuarioPerteneceAlGrupoException(){
        super("El usuario ya pertenece a este grupo");
    }
}
