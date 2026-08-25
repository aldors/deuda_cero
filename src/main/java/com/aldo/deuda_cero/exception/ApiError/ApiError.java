package com.aldo.deuda_cero.exception.ApiError;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;

@Getter
public class ApiError {
    
    private int status; // Codigo de estado HTTP
    private String message; // Mensaje de error
    private LocalDateTime timestamp; // Fecha del error
    private List<String> errors; // Lista de errores detallados (opcional)

    public ApiError(int status, String message, List<String> errors) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
}
