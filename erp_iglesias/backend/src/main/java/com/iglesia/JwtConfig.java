// ========================================================================
// CLASE DE CONFIGURACIÓN JWT (PARTE DEL CAMBIO 5: CONFIGURACIÓN EXTERNALIZADA)
// ========================================================================
/**
 * Configuración JWT externalizada.
 * ADR-010: Configuración Externalizada (12-Factor App)
 * Beneficios: Configuración inyectada desde application.yml
 * Secretos en variables de entorno, no hardcodeados
 */
package com.iglesia;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    private String secret;
    private int expirationMinutes;

    public JwtConfig() {}

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(int expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    // Método helper para obtener expiración en milisegundos
    public long getExpirationMillis() {
        return (long) expirationMinutes * 60 * 1000;
    }
}