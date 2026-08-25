package com.aldo.deuda_cero.exception;

public class PagoNoPuedeSuperarLaDeudaPendiente extends RuntimeException{
    
    public PagoNoPuedeSuperarLaDeudaPendiente(){
        super("El monto a pagar no debe superar la deuda pendiente");
    }
}
