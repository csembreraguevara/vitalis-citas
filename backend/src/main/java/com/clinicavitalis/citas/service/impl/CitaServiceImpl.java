package com.clinicavitalis.citas.service.impl;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.dto.CitaRequestDTO;
import com.clinicavitalis.citas.entity.CanalCita;
import com.clinicavitalis.citas.entity.Cita;
import com.clinicavitalis.citas.entity.EstadoCita;
import com.clinicavitalis.citas.entity.Medico;
import com.clinicavitalis.citas.entity.Paciente;
import com.clinicavitalis.citas.entity.Rol;
import com.clinicavitalis.citas.entity.Usuario;
import com.clinicavitalis.citas.exception.HorarioOcupadoException;
import com.clinicavitalis.citas.exception.RecursoNoEncontradoException;
import com.clinicavitalis.citas.exception.ReglaNegocioException;
import com.clinicavitalis.citas.repository.CitaRepository;
import com.clinicavitalis.citas.repository.MedicoRepository;
import com.clinicavitalis.citas.repository.PacienteRepository;
import com.clinicavitalis.citas.repository.UsuarioRepository;
import com.clinicavitalis.citas.service.CitaService;
import com.clinicavitalis.citas.service.DisponibilidadService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Reglas del agendamiento:
 * <ul>
 *   <li>RN01: un turno solo puede tener una cita activa.</li>
 *   <li>RN02: la reserva del paciente queda PENDIENTE 5 minutos hasta que la confirme.</li>
 *   <li>RN03: se cancela hasta 2 horas antes.</li>
 *   <li>RN04: no se reserva en el pasado ni a más de 60 días.</li>
 *   <li>RN05: máximo 3 citas futuras activas por paciente.</li>
 *   <li>RN06: la hora debe ser un turno del horario del médico.</li>
 * </ul>
 */
@Service
public class CitaServiceImpl implements CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaServiceImpl.class);

    static final int MINUTOS_RESERVA = 5;
    static final int MAX_CITAS_FUTURAS = 3;
    static final int HORAS_MINIMAS_CANCELACION = 2;

    private static final Set<EstadoCita> OCUPAN_TURNO = EnumSet.of(
            EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.ATENDIDA, EstadoCita.NO_ASISTIO);
    private static final Set<EstadoCita> FUTURAS_ACTIVAS = EnumSet.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA);

    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final DisponibilidadService disponibilidadService;
    private final Clock clock;

    public CitaServiceImpl(CitaRepository citaRepository, MedicoRepository medicoRepository,
                           PacienteRepository pacienteRepository, UsuarioRepository usuarioRepository,
                           DisponibilidadService disponibilidadService, Clock clock) {
        this.citaRepository = citaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.disponibilidadService = disponibilidadService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CitaDTO reservar(CitaRequestDTO solicitud, String username, Rol rol) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        citaRepository.expirarReservasVencidas(ahora);

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Paciente paciente = pacienteDeLaReserva(solicitud, username, rol);
        Medico medico = medicoRepository.buscarConEspecialidad(solicitud.idMedico())
                .filter(Medico::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado"));

        LocalDateTime fechaHora = LocalDateTime.of(solicitud.fecha(), solicitud.hora());
        if (!fechaHora.isAfter(ahora)) {
            throw new ReglaNegocioException("No se pueden reservar citas en fechas u horas pasadas.");
        }
        if (solicitud.fecha().isAfter(ahora.toLocalDate().plusDays(DisponibilidadServiceImpl.MAX_DIAS_ANTICIPACION))) {
            throw new ReglaNegocioException("Solo se puede reservar con un máximo de 60 días de anticipación.");
        }
        if (!disponibilidadService.turnosDelDia(medico, solicitud.fecha()).contains(solicitud.hora())) {
            throw new ReglaNegocioException("La hora elegida no corresponde a un turno del médico.");
        }
        if (citaRepository.existsByMedicoIdAndFechaAndHoraAndEstadoIn(
                medico.getId(), solicitud.fecha(), solicitud.hora(), OCUPAN_TURNO)) {
            throw new HorarioOcupadoException();
        }
        long futuras = citaRepository.contarFuturas(paciente.getId(), FUTURAS_ACTIVAS,
                ahora.toLocalDate(), ahora.toLocalTime());
        if (futuras >= MAX_CITAS_FUTURAS) {
            throw new ReglaNegocioException("El paciente ya tiene " + MAX_CITAS_FUTURAS + " citas activas pendientes.");
        }

        CanalCita canal = rol == Rol.PACIENTE ? CanalCita.PORTAL : CanalCita.RECEPCION;
        Cita cita = new Cita(paciente, medico, solicitud.fecha(), solicitud.hora(), canal, usuario);
        cita.setMotivo(StringUtils.trimToNull(StringUtils.normalizeSpace(solicitud.motivo())));
        if (rol == Rol.PACIENTE) {
            cita.setExpiraEn(ahora.plusMinutes(MINUTOS_RESERVA));
        } else {
            cita.confirmar();
        }

        try {
            citaRepository.saveAndFlush(cita);
        } catch (DataIntegrityViolationException ex) {
            // Otra persona tomó el turno entre la validación y el guardado (índice uk_cita_slot).
            throw new HorarioOcupadoException();
        }
        log.info("Cita {} registrada por {} ({}) para {} {}", cita.getId(), username, canal,
                cita.getFecha(), cita.getHora());
        return CitaDTO.de(cita);
    }

    @Override
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public CitaDTO confirmar(Long idCita, String username) {
        citaRepository.expirarReservasVencidas(LocalDateTime.now(clock));
        Cita cita = buscarCita(idCita);
        exigirDueno(cita, username);
        if (cita.getEstado() == EstadoCita.EXPIRADA) {
            throw new ReglaNegocioException("La reserva venció porque pasaron más de "
                    + MINUTOS_RESERVA + " minutos. Elige el horario otra vez.");
        }
        try {
            cita.confirmar();
        } catch (IllegalStateException ex) {
            throw new ReglaNegocioException(ex.getMessage());
        }
        log.info("Cita {} confirmada por {}", cita.getId(), username);
        return CitaDTO.de(cita);
    }

    @Override
    @Transactional
    public CitaDTO cancelar(Long idCita, String motivo, String username, Rol rol) {
        Cita cita = buscarCita(idCita);
        if (rol == Rol.PACIENTE) {
            exigirDueno(cita, username);
        }
        LocalDateTime limite = cita.getFechaHora().minusHours(HORAS_MINIMAS_CANCELACION);
        if (LocalDateTime.now(clock).isAfter(limite)) {
            throw new ReglaNegocioException("Las citas solo se pueden cancelar hasta "
                    + HORAS_MINIMAS_CANCELACION + " horas antes.");
        }
        try {
            cita.cancelar(StringUtils.normalizeSpace(motivo));
        } catch (IllegalStateException ex) {
            throw new ReglaNegocioException(ex.getMessage());
        }
        log.info("Cita {} cancelada por {}", cita.getId(), username);
        return CitaDTO.de(cita);
    }

    @Override
    @Transactional
    public List<CitaDTO> misCitas(String username) {
        citaRepository.expirarReservasVencidas(LocalDateTime.now(clock));
        Paciente paciente = pacienteRepository.findByUsuarioUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tu cuenta no tiene datos de paciente."));
        return citaRepository.buscarPorPaciente(paciente.getId()).stream()
                .map(CitaDTO::de)
                .toList();
    }

    private Paciente pacienteDeLaReserva(CitaRequestDTO solicitud, String username, Rol rol) {
        if (rol == Rol.PACIENTE) {
            return pacienteRepository.findByUsuarioUsername(username)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Tu cuenta no tiene datos de paciente."));
        }
        if (solicitud.idPaciente() == null) {
            throw new ReglaNegocioException("Indica el paciente de la cita.");
        }
        return pacienteRepository.findById(solicitud.idPaciente())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));
    }

    private Cita buscarCita(Long idCita) {
        return citaRepository.buscarDetalle(idCita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));
    }

    /** Un paciente solo puede tocar sus propias citas: si no es suya, 403. */
    private void exigirDueno(Cita cita, String username) {
        Usuario dueno = cita.getPaciente().getUsuario();
        if (dueno == null || !dueno.getUsername().equals(username)) {
            throw new AccessDeniedException("La cita no pertenece a tu cuenta");
        }
    }
}
