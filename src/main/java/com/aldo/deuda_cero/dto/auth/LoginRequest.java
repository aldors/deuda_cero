package com.aldo.deuda_cero.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    
    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "El correo electronico debe ser valido")
    private String email;

    @NotBlank(message = "La contrasela es obligatoria")
    @Size(min = 6,message = "La contraseña deben tener al menos 6 caracteres")
    private String password;
}
