package com.aldo.deuda_cero.exception;

public class MiembroPagadorNoPerteneceAlGrupoException extends RuntimeException{
    
    public MiembroPagadorNoPerteneceAlGrupoException(){
        super("El miembro deudor o pagador no pertenece a este grupo");
    }
}
