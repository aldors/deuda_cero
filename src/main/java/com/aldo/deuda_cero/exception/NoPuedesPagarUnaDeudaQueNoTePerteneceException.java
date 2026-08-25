package com.aldo.deuda_cero.exception;

public class NoPuedesPagarUnaDeudaQueNoTePerteneceException extends RuntimeException{
    
    public NoPuedesPagarUnaDeudaQueNoTePerteneceException(){
        super("Lo sentimos, No puedes pagar una deuda que no te pertenece");
    }
}
