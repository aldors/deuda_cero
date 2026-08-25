package com.aldo.deuda_cero.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

//import com.aldo.deuda_cero.entity.Usuario;
//import com.aldo.deuda_cero.repository.UsuarioRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expirationAccessToken}")
    private long jwtExpirationAccessToken;

    @Value("${jwt.expirationRefreshToken}")
    private long jwtExpirationRefreshToken;

    //private final UsuarioRepository usuarioRepository;

    public String generateAccessToken(UserDetails userDetails) {
        //Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                // Esto sirve para poder ver el nombre del usuario en la pestaña de perfil
                //.claim("nombre", usuario.getNombre().toUpperCase())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationAccessToken))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationRefreshToken))
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        try {     
            final String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}

