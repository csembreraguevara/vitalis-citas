package com.clinicavitalis.citas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj de la aplicación en la zona horaria de la clínica. Se inyecta en
 * lugar de llamar a {@code LocalDateTime.now()} para que las pruebas puedan
 * fijar la hora (TDD).
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Lima"));
    }
}
