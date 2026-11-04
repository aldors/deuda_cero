package com.aldo.deuda_cero.exception;

public class MontoParticipanteInvalidoException extends RuntimeException{

    public MontoParticipanteInvalidoException(){
        super("El monto de cada participante debe ser mayor que cero");
    }
}
