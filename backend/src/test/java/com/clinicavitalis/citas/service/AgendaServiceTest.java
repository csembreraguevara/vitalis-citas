package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.entity.CanalCita;
import com.clinicavitalis.citas.entity.Cita;
import com.clinicavitalis.citas.entity.Especialidad;
import com.clinicavitalis.citas.entity.Medico;
import com.clinicavitalis.citas.entity.Paciente;
import com.clinicavitalis.citas.entity.Rol;
import com.clinicavitalis.citas.entity.Sexo;
import com.clinicavitalis.citas.exception.ReglaNegocioException;
import com.clinicavitalis.citas.repository.AgendaRepository;
import com.clinicavitalis.citas.service.impl.AgendaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** Reglas del registro de asistencia (RF013). Reloj fijo: lunes 16/11/2026, 10:00 (Lima). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgendaServiceTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-16T15:00:00Z"), ZoneId.of("America/Lima"));
    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);

    @Mock private AgendaRepository agendaRepository;

    private AgendaService agendaService;
    private Medico ramirez;
    private Medico salazar;

    @BeforeEach
    void setUp() {
        agendaService = new AgendaServiceImpl(agendaRepository, RELOJ);
        Especialidad general = new Especialidad("Medicina general", "", 30);
        ramirez = new Medico(general, "045218", "Jorge", "Ramírez", "C-104");
        salazar = new Medico(general, "051733", "Carmen", "Salazar", "C-105");
        ReflectionTestUtils.setField(ramirez, "id", 1L);
        ReflectionTestUtils.setField(salazar, "id", 2L);
        when(agendaRepository.medicoDeUsuario("jramirez")).thenReturn(Optional.of(ramirez));
    }

    private Cita citaConfirmada(Medico medico, LocalDate fecha) {
        Paciente paciente = new Paciente("45781203", "Ana", "Torres", LocalDate.of(1990, 4, 12), Sexo.F, "a@b.com");
        Cita cita = new Cita(paciente, medico, fecha, LocalTime.of(9, 0), CanalCita.RECEPCION, null);
        cita.confirmar();
        when(agendaRepository.buscarCita(7L)).thenReturn(Optional.of(cita));
        return cita;
    }

    @Test
    @DisplayName("El médico marca como atendida una cita confirmada de su agenda")
    void medicoMarcaAtendida() {
        citaConfirmada(ramirez, HOY);

        CitaDTO resultado = agendaService.marcarAtendida(7L, "jramirez", Rol.MEDICO);

        assertThat(resultado.estado()).isEqualTo("ATENDIDA");
    }

    @Test
    @DisplayName("Un médico no puede marcar citas de otro médico (403)")
    void medicoNoMarcaCitaAjena() {
        citaConfirmada(salazar, HOY);

        assertThatThrownBy(() -> agendaService.marcarAtendida(7L, "jramirez", Rol.MEDICO))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("No se registra asistencia antes del día de la cita")
    void noMarcaCitaFutura() {
        citaConfirmada(ramirez, HOY.plusDays(1));

        assertThatThrownBy(() -> agendaService.marcarNoAsistio(7L, "recepcion01", Rol.RECEPCIONISTA))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("Una cita ya atendida no se vuelve a marcar")
    void noMarcaDosVeces() {
        citaConfirmada(ramirez, HOY).marcarAtendida();

        assertThatThrownBy(() -> agendaService.marcarNoAsistio(7L, "recepcion01", Rol.RECEPCIONISTA))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
