package com.aldo.deuda_cero.mapper;

import org.springframework.stereotype.Component;

import com.aldo.deuda_cero.dto.auth.RegistroRequest;
import com.aldo.deuda_cero.dto.auth.RegistroResponse;
import com.aldo.deuda_cero.entity.enums.Role;
import com.aldo.deuda_cero.entity.Usuario;

@Component
public class ResgistroMapper {
    
    public static Usuario toEntity(RegistroRequest registroRequest){

        return Usuario.builder()
                .nombre(registroRequest.getNombre())
                .apellido(registroRequest.getApellido())
                .email(registroRequest.getEmail())
                .password(registroRequest.getPassword())
                .role(Role.USER)
                .activo(true)
                .build();
    }

    public static RegistroResponse toResponse(Usuario usuario){

        return new RegistroResponse(
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getRole().name()
        );
    }
}
