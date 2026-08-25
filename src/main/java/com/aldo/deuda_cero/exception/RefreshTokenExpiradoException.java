package com.aldo.deuda_cero.exception;

public class RefreshTokenExpiradoException extends RuntimeException{
    
    public RefreshTokenExpiradoException(){
        super("El refresh token ha expirado");
    }
}
