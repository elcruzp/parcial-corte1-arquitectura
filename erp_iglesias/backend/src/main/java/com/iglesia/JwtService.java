package com.iglesia;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
// import org.springframework.beans.factory.annotation.Value; // Removido - ahora usa JwtConfig
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtService {

    // ========================================================================
    // CAMBIO 5: CONFIGURACIÓN EXTERNALIZADA CON JWT CONFIG CLASS
    // ========================================================================
    // ANTES: @Value("${app.jwt.secret}") String secret (hardcoded injection)
    // DESPUÉS: JwtConfig config (typed configuration class)
    // Beneficio: Type safety, validation, mejor testability
    // ========================================================================

    private final Key key;
    private final int expirationMinutes;

    public JwtService(JwtConfig config) {  // Inyección de JwtConfig (Cambio 5)
        this.key = Keys.hmacShaKeyFor(config.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = config.getExpirationMinutes();
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("role", user.getRole().name())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        Object role = getClaims(token).get("role");
        return role == null ? null : role.toString();
    }

    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
