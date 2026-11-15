package com.clinicavitalis.citas.service.impl;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.entity.Cita;
import com.clinicavitalis.citas.entity.Medico;
import com.clinicavitalis.citas.entity.Rol;
import com.clinicavitalis.citas.exception.RecursoNoEncontradoException;
import com.clinicavitalis.citas.exception.ReglaNegocioException;
import com.clinicavitalis.citas.repository.AgendaRepository;
import com.clinicavitalis.citas.service.AgendaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Agenda diaria (RF013):
 * <ul>
 *   <li>El médico solo ve y marca sus propias citas (si no, 403).</li>
 *   <li>La asistencia se registra el día de la cita o después, nunca antes.</li>
 *   <li>Solo una cita CONFIRMADA pasa a ATENDIDA o NO_ASISTIO.</li>
 * </ul>
 */
@Service
public class AgendaServiceImpl implements AgendaService {

    private static final Logger log = LoggerFactory.getLogger(AgendaServiceImpl.class);

    private final AgendaRepository agendaRepository;
    private final Clock clock;

    public AgendaServiceImpl(AgendaRepository agendaRepository, Clock clock) {
        this.agendaRepository = agendaRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaDTO> citasDelDia(LocalDate fecha, Long idMedico, String username, Rol rol) {
        Long filtro = rol == Rol.MEDICO ? medicoDe(username).getId() : idMedico;
        return agendaRepository.citasDelDia(fecha, filtro).stream()
                .map(CitaDTO::de)
                .toList();
    }

    @Override
    @Transactional
    public CitaDTO marcarAtendida(Long idCita, String username, Rol rol) {
        return registrar(idCita, username, rol, Cita::marcarAtendida, "atendida");
    }

    @Override
    @Transactional
    public CitaDTO marcarNoAsistio(Long idCita, String username, Rol rol) {
        return registrar(idCita, username, rol, Cita::marcarNoAsistio, "no asistió");
    }

    private CitaDTO registrar(Long idCita, String username, Rol rol, Consumer<Cita> accion, String descripcion) {
        Cita cita = agendaRepository.buscarCita(idCita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));
        if (rol == Rol.MEDICO && !cita.getMedico().getId().equals(medicoDe(username).getId())) {
            throw new AccessDeniedException("La cita no es de tu agenda");
        }
        if (cita.getFecha().isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("La asistencia se registra el día de la cita, no antes.");
        }
        try {
            accion.accept(cita);
        } catch (IllegalStateException ex) {
            throw new ReglaNegocioException("Solo se registra la asistencia de citas confirmadas.");
        }
        log.info("Cita {} marcada como {} por {}", cita.getId(), descripcion, username);
        return CitaDTO.de(cita);
    }

    private Medico medicoDe(String username) {
        return agendaRepository.medicoDeUsuario(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tu cuenta no está asociada a un médico."));
    }
}
