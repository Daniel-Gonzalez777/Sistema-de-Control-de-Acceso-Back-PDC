package com.parquecafe.accesoapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Genera y valida el token que recibe el usuario al iniciar sesión.
 * El token viaja luego en cada petición (header "Authorization: Bearer ...")
 * y lleva adentro el username y el rol, para que el backend sepa quién
 * pregunta sin tener que consultar la base de datos en cada petición.
 */
@Service
public class JwtService {

    private final Key key;
    private static final long EXPIRACION_MS = 1000L * 60 * 60 * 12; // 12 horas

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String username, String rol) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + EXPIRACION_MS);

        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .setIssuedAt(ahora)
                .setExpiration(expira)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Lanza una excepción si el token es inválido o expiró.
     */
    public Claims validarYObtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}