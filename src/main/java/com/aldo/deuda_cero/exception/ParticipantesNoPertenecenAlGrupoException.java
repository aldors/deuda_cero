package com.aldo.deuda_cero.exception;

public class ParticipantesNoPertenecenAlGrupoException extends RuntimeException{
    
    public ParticipantesNoPertenecenAlGrupoException(){
        super("Uno o mas participantes no pertenecen a este grupo");
    }
}
