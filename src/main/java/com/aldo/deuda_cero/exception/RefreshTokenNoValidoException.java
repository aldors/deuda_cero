package com.aldo.deuda_cero.exception;

public class RefreshTokenNoValidoException extends RuntimeException{
    
    public RefreshTokenNoValidoException(){
        super("El refresh token no es válido");
    }
}
