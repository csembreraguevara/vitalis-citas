package com.clinicavitalis.citas.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas unitarias de la generación y validación del JWT (RF002, RNF001). */
class JwtServiceTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final Instant INICIO = Instant.parse("2026-10-09T15:00:00Z");
    private static final String SECRETO = "dml0YWxpcy1jaXRhcy1jbGF2ZS1kZS1wcnVlYmFzLXVuaXRhcmlhcy0yMDI2LTEyMzQ=";

    private JwtService servicioEn(Instant instante) {
        return new JwtService(SECRETO, 60, Clock.fixed(instante, LIMA));
    }

    @Test
    @DisplayName("Un token recién generado es válido y conserva el usuario y el rol")
    void tokenValido() {
        JwtService jwt = servicioEn(INICIO);

        String token = jwt.generarToken("admin", "ADMIN");

        assertThat(jwt.validar(token)).hasValueSatisfying(datos -> {
            assertThat(datos.username()).isEqualTo("admin");
            assertThat(datos.rol()).isEqualTo("ADMIN");
        });
    }

    @Test
    @DisplayName("Un token vencido (más de 60 minutos) se rechaza")
    void tokenVencido() {
        String token = servicioEn(INICIO).generarToken("admin", "ADMIN");

        JwtService unaHoraDespues = servicioEn(INICIO.plus(Duration.ofMinutes(61)));

        assertThat(unaHoraDespues.validar(token)).isEmpty();
    }

    @Test
    @DisplayName("Un token alterado (firma inválida) se rechaza")
    void tokenAlterado() {
        JwtService jwt = servicioEn(INICIO);
        String token = jwt.generarToken("paciente", "PACIENTE");
        String alterado = token.substring(0, token.length() - 2)
                + (token.endsWith("AA") ? "BB" : "AA");

        assertThat(jwt.validar(alterado)).isEmpty();
    }

    @Test
    @DisplayName("Un texto que no es un JWT se rechaza")
    void textoNoEsToken() {
        assertThat(servicioEn(INICIO).validar("esto-no-es-un-token")).isEmpty();
    }
}
