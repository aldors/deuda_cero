package com.aldo.deuda_cero.exception;

public class UsuarioNoEncontradoException extends RuntimeException{
    
    public UsuarioNoEncontradoException(){
        super("El usuario no ha sido encontrado");
    }
}
