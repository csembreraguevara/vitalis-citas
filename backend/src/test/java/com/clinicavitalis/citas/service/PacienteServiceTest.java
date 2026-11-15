package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.PacienteRequestDTO;
import com.clinicavitalis.citas.dto.PacienteResumenDTO;
import com.clinicavitalis.citas.entity.Paciente;
import com.clinicavitalis.citas.entity.Sexo;
import com.clinicavitalis.citas.exception.ReglaNegocioException;
import com.clinicavitalis.citas.repository.PacienteRepository;
import com.clinicavitalis.citas.service.impl.PacienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Registro de pacientes por recepción (RF004). */
@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-16T15:00:00Z"), ZoneId.of("America/Lima"));

    @Mock private PacienteRepository pacienteRepository;

    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        pacienteService = new PacienteServiceImpl(pacienteRepository, RELOJ);
    }

    private PacienteRequestDTO solicitud(String dni, LocalDate nacimiento) {
        return new PacienteRequestDTO(dni, "  maría   del CARMEN ", "QUISPE  rojas", nacimiento, Sexo.F,
                "987654321", " Maria.Quispe@Correo.com ", null);
    }

    @Test
    @DisplayName("Registra al paciente con nombres ordenados y correo en minúsculas")
    void registraPaciente() {
        when(pacienteRepository.findByDni("80385685")).thenReturn(Optional.empty());

        PacienteResumenDTO resultado = pacienteService.registrar(
                solicitud("80385685", LocalDate.of(1995, 3, 8)), "recepcion01");

        ArgumentCaptor<Paciente> guardado = ArgumentCaptor.forClass(Paciente.class);
        verify(pacienteRepository).saveAndFlush(guardado.capture());
        assertThat(resultado.nombreCompleto()).isEqualTo("María Del Carmen Quispe Rojas");
        assertThat(guardado.getValue().getEmail()).isEqualTo("maria.quispe@correo.com");
    }

    @Test
    @DisplayName("No permite dos pacientes con el mismo DNI")
    void rechazaDniRepetido() {
        Paciente existente = new Paciente("45120876", "Luis", "García", LocalDate.of(1985, 11, 3), Sexo.M, "l@c.com");
        when(pacienteRepository.findByDni("45120876")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> pacienteService.registrar(
                solicitud("45120876", LocalDate.of(1995, 3, 8)), "recepcion01"))
                .isInstanceOf(ReglaNegocioException.class);
        verify(pacienteRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Rechaza una fecha de nacimiento de más de 120 años")
    void rechazaEdadImposible() {
        when(pacienteRepository.findByDni("80385685")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.registrar(
                solicitud("80385685", LocalDate.of(1890, 1, 1)), "recepcion01"))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
