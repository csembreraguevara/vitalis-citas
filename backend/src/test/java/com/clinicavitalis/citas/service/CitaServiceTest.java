package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.dto.CitaRequestDTO;
import com.clinicavitalis.citas.entity.Cita;
import com.clinicavitalis.citas.entity.Especialidad;
import com.clinicavitalis.citas.entity.Medico;
import com.clinicavitalis.citas.entity.Paciente;
import com.clinicavitalis.citas.entity.Rol;
import com.clinicavitalis.citas.entity.Sexo;
import com.clinicavitalis.citas.entity.Usuario;
import com.clinicavitalis.citas.exception.HorarioOcupadoException;
import com.clinicavitalis.citas.exception.ReglaNegocioException;
import com.clinicavitalis.citas.repository.CitaRepository;
import com.clinicavitalis.citas.repository.MedicoRepository;
import com.clinicavitalis.citas.repository.PacienteRepository;
import com.clinicavitalis.citas.repository.UsuarioRepository;
import com.clinicavitalis.citas.service.impl.CitaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reglas de la reserva de citas (RN01, RN02, RN04, RN05, RN06).
 * Reloj fijo: lunes 16/11/2026 a las 10:00 (hora de Lima).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CitaServiceTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-16T15:00:00Z"), LIMA);
    private static final LocalDateTime AHORA = LocalDateTime.now(RELOJ);
    private static final LocalDate MIERCOLES = LocalDate.of(2026, 11, 18);
    private static final LocalTime NUEVE = LocalTime.of(9, 0);

    @Mock private CitaRepository citaRepository;
    @Mock private MedicoRepository medicoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DisponibilidadService disponibilidadService;

    private CitaService citaService;
    private Medico medico;

    @BeforeEach
    void setUp() {
        citaService = new CitaServiceImpl(citaRepository, medicoRepository, pacienteRepository,
                usuarioRepository, disponibilidadService, RELOJ);

        medico = new Medico(new Especialidad("Medicina general", "", 30), "045218", "Jorge", "Ramírez", "C-104");
        Usuario usuario = new Usuario("ana.torres@correo.com", "hash", Rol.PACIENTE);
        Paciente paciente = new Paciente("45781203", "Ana", "Torres", LocalDate.of(1990, 4, 12), Sexo.F,
                "ana.torres@correo.com");
        paciente.setUsuario(usuario);

        when(usuarioRepository.findByUsername("ana.torres@correo.com")).thenReturn(Optional.of(usuario));
        when(pacienteRepository.findByUsuarioUsername("ana.torres@correo.com")).thenReturn(Optional.of(paciente));
        when(medicoRepository.buscarConEspecialidad(1L)).thenReturn(Optional.of(medico));
        when(disponibilidadService.turnosDelDia(any(), any())).thenReturn(List.of(NUEVE, LocalTime.of(9, 30)));
    }

    private CitaDTO reservar(LocalDate fecha, LocalTime hora) {
        return citaService.reservar(new CitaRequestDTO(1L, fecha, hora, "Control", null),
                "ana.torres@correo.com", Rol.PACIENTE);
    }

    @Test
    @DisplayName("RN02: la reserva del paciente queda PENDIENTE y vence en 5 minutos")
    void reservaDelPacienteQuedaPendiente() {
        CitaDTO cita = reservar(MIERCOLES, NUEVE);

        assertThat(cita.estado()).isEqualTo("PENDIENTE");
        assertThat(cita.canal()).isEqualTo("PORTAL");
        assertThat(cita.expiraEn()).isEqualTo(AHORA.plusMinutes(5));
        verify(citaRepository).saveAndFlush(any(Cita.class));
    }

    @Test
    @DisplayName("RN04: rechaza una hora que ya pasó")
    void rechazaFechaPasada() {
        assertThatThrownBy(() -> reservar(AHORA.toLocalDate(), NUEVE))
                .isInstanceOf(ReglaNegocioException.class);
        verify(citaRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("RN04: rechaza reservar con más de 60 días de anticipación")
    void rechazaMasDeSesentaDias() {
        assertThatThrownBy(() -> reservar(AHORA.toLocalDate().plusDays(61), NUEVE))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("RN06: rechaza una hora que no es turno del médico")
    void rechazaHoraFueraDeTurno() {
        assertThatThrownBy(() -> reservar(MIERCOLES, LocalTime.of(9, 10)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("RN01: un turno ocupado responde HorarioOcupado (409)")
    void rechazaTurnoOcupado() {
        when(citaRepository.existsByMedicoIdAndFechaAndHoraAndEstadoIn(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservar(MIERCOLES, NUEVE)).isInstanceOf(HorarioOcupadoException.class);
    }

    @Test
    @DisplayName("RN05: rechaza una cuarta cita futura activa")
    void rechazaCuartaCita() {
        when(citaRepository.contarFuturas(any(), any(), any(), any())).thenReturn(3L);

        assertThatThrownBy(() -> reservar(MIERCOLES, NUEVE)).isInstanceOf(ReglaNegocioException.class);
        verify(citaRepository, never()).saveAndFlush(any());
    }
}
