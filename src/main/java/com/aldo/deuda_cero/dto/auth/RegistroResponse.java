package com.aldo.deuda_cero.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistroResponse {
    
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
}
