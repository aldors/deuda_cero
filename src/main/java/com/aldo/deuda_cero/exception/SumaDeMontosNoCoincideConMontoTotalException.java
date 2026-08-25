package com.aldo.deuda_cero.exception;

public class SumaDeMontosNoCoincideConMontoTotalException extends RuntimeException{
    
    public SumaDeMontosNoCoincideConMontoTotalException(){
        super("La suma de los montos no coincide con el monto total");
    }
}
