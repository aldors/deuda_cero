package com.aldo.deuda_cero.securityTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.repository.UsuarioRepository;
import com.aldo.deuda_cero.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deberiaLanzarInsufficientAuthenticationExceptionSinAutenticacion() {

        assertThrows(
            InsufficientAuthenticationException.class,
            () -> currentUserService.obtenerUsuarioActual()
        );
    }

    @Test
    void deberiaLanzarInsufficientAuthenticationExceptionConUsuarioAnonimo() {

        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(
            InsufficientAuthenticationException.class,
            () -> currentUserService.obtenerUsuarioActual()
        );
    }

    @Test
    void deberiaObtenerUsuarioAutenticado() {

        Usuario usuario = Usuario.builder()
            .id(1L)
            .nombre("Aldo")
            .apellido("Ruiz")
            .email("aldo@test.com")
            .password("password")
            .build();

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                usuario.getEmail(),
                null,
                AuthorityUtils.createAuthorityList("ROLE_USER")
            )
        );

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        Usuario resultado = currentUserService.obtenerUsuarioActual();

        assertEquals(usuario.getId(), resultado.getId());
        assertEquals(usuario.getEmail(), resultado.getEmail());
        verify(usuarioRepository).findByEmail(usuario.getEmail());
    }
}
