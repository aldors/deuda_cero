package com.aldo.deuda_cero.exception;

public class MiembroNoEncontradoException extends RuntimeException {

    public MiembroNoEncontradoException() {
        super("El miembro no pertenece al grupo o no está activo");
    }
}
