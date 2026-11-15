package com.clinicavitalis.citas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Genera y valida los tokens JWT (RFC 7519) firmados con HMAC-SHA.
 * El token lleva el usuario (subject), su rol y una vigencia de 60 minutos.
 */
@Service
public class JwtService {

    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final Duration vigencia;
    private final Clock clock;

    public JwtService(@Value("${app.jwt.secret}") String secretoBase64,
                      @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos,
                      Clock clock) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.vigencia = Duration.ofMinutes(expiracionMinutos);
        this.clock = clock;
    }

    /** Genera un token firmado para el usuario y rol indicados. */
    public String generarToken(String username, String rol) {
        Instant ahora = clock.instant();
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROL, rol)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(vigencia)))
                .signWith(clave)
                .compact();
    }

    /**
     * Valida la firma y la vigencia del token.
     *
     * @return los datos del usuario si el token es válido; vacío si fue
     *         alterado, expiró o tiene un formato incorrecto
     */
    public Optional<TokenValido> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new TokenValido(claims.getSubject(), claims.get(CLAIM_ROL, String.class)));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long getVigenciaEnSegundos() {
        return vigencia.toSeconds();
    }

    /** Datos extraídos de un token válido. */
    public record TokenValido(String username, String rol) {
    }
}
