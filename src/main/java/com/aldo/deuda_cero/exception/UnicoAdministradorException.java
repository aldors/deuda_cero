package com.aldo.deuda_cero.exception;

public class UnicoAdministradorException extends RuntimeException {
    public UnicoAdministradorException(){
        super("No puedes abandonar el grupo porque eres el único administrador. Asigna otro administrador");
    }
}
