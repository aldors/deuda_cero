package com.aldo.deuda_cero.exception;

public class RefreshTokenNoEncontradoException extends RuntimeException{
    
    public RefreshTokenNoEncontradoException(){
        super("El refresh token no ha sido encontrado");
    }
}
