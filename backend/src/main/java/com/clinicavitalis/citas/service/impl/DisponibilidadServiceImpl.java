package com.clinicavitalis.citas.service.impl;

import com.clinicavitalis.citas.dto.DisponibilidadDTO;
import com.clinicavitalis.citas.dto.TurnoDTO;
import com.clinicavitalis.citas.entity.EstadoCita;
import com.clinicavitalis.citas.entity.HorarioMedico;
import com.clinicavitalis.citas.entity.Medico;
import com.clinicavitalis.citas.exception.RecursoNoEncontradoException;
import com.clinicavitalis.citas.repository.CitaRepository;
import com.clinicavitalis.citas.repository.HorarioMedicoRepository;
import com.clinicavitalis.citas.repository.MedicoRepository;
import com.clinicavitalis.citas.service.DisponibilidadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Arma los turnos de un médico a partir de su horario semanal y la duración
 * de su especialidad (RN06), y marca como no disponibles los ocupados (RN01),
 * los pasados y los que superan los 60 días de anticipación (RN04).
 */
@Service
public class DisponibilidadServiceImpl implements DisponibilidadService {

    static final int MAX_DIAS_ANTICIPACION = 60;

    private static final Set<EstadoCita> OCUPAN_TURNO = EnumSet.of(
            EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.ATENDIDA, EstadoCita.NO_ASISTIO);

    private final MedicoRepository medicoRepository;
    private final HorarioMedicoRepository horarioRepository;
    private final CitaRepository citaRepository;
    private final Clock clock;

    public DisponibilidadServiceImpl(MedicoRepository medicoRepository, HorarioMedicoRepository horarioRepository,
                                     CitaRepository citaRepository, Clock clock) {
        this.medicoRepository = medicoRepository;
        this.horarioRepository = horarioRepository;
        this.citaRepository = citaRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public DisponibilidadDTO consultar(Long idMedico, LocalDate fecha) {
        Medico medico = medicoRepository.buscarConEspecialidad(idMedico)
                .filter(Medico::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado"));

        LocalDateTime ahora = LocalDateTime.now(clock);
        citaRepository.expirarReservasVencidas(ahora);

        Set<LocalTime> ocupadas = new HashSet<>(citaRepository.horasOcupadas(idMedico, fecha, OCUPAN_TURNO));
        boolean dentroDelPlazo = !fecha.isAfter(ahora.toLocalDate().plusDays(MAX_DIAS_ANTICIPACION));

        List<TurnoDTO> turnos = turnosDelDia(medico, fecha).stream()
                .map(hora -> new TurnoDTO(hora, dentroDelPlazo
                        && !ocupadas.contains(hora)
                        && LocalDateTime.of(fecha, hora).isAfter(ahora)))
                .toList();

        return new DisponibilidadDTO(medico.getId(), medico.getNombreCompleto(),
                medico.getEspecialidad().getNombre(), medico.getConsultorio(), fecha,
                medico.getEspecialidad().getDuracionCitaMin(), turnos);
    }

    @Override
    public List<LocalTime> turnosDelDia(Medico medico, LocalDate fecha) {
        int duracion = medico.getEspecialidad().getDuracionCitaMin();
        List<LocalTime> turnos = new ArrayList<>();
        if (duracion <= 0) {
            return turnos;
        }
        for (HorarioMedico bloque : horarioRepository.findByMedicoIdAndDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
                medico.getId(), fecha.getDayOfWeek().getValue())) {
            LocalTime hora = bloque.getHoraInicio();
            while (true) {
                LocalTime fin = hora.plusMinutes(duracion);
                if (fin.isBefore(hora) || fin.isAfter(bloque.getHoraFin())) {
                    break; // el turno no entra completo en el bloque (o pasaría la medianoche)
                }
                turnos.add(hora);
                hora = fin;
            }
        }
        return turnos;
    }
}
