package com.aldo.deuda_cero.exception;

public class MontoAPagarDebeSerMayorQueCeroException extends RuntimeException{

    public MontoAPagarDebeSerMayorQueCeroException(){
        super("El monto apagar debe ser mayor que cero");
    }
}
