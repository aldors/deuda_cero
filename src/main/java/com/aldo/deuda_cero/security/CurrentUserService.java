package com.aldo.deuda_cero.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.exception.UsuarioNoEncontradoException;
import com.aldo.deuda_cero.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UsuarioRepository usuarioRepository;
    
    public Usuario obtenerUsuarioActual(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsuarioNoEncontradoException());
    }

    public Long obtenerIdUsuarioActual(){

        return obtenerUsuarioActual().getId();
    }

    public String obtenerEmailUsuarioActual(){

        return obtenerUsuarioActual().getEmail();
    }
}
