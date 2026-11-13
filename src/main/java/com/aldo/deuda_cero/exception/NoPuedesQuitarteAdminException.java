package com.aldo.deuda_cero.exception;

public class NoPuedesQuitarteAdminException extends RuntimeException {

    public NoPuedesQuitarteAdminException() {
        super("No puedes quitarte el rol de administrador a ti mismo");
    }
}
