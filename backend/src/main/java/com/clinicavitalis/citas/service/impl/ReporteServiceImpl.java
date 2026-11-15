package com.clinicavitalis.citas.service.impl;

import com.clinicavitalis.citas.dto.ConteoEstadoDTO;
import com.clinicavitalis.citas.dto.OcupacionDTO;
import com.clinicavitalis.citas.entity.EstadoCita;
import com.clinicavitalis.citas.exception.ReglaNegocioException;
import com.clinicavitalis.citas.repository.CitaRepository;
import com.clinicavitalis.citas.service.ReporteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    static final int MAX_DIAS_RANGO = 366;

    private final CitaRepository citaRepository;

    public ReporteServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public List<OcupacionDTO> ocupacion(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la inicial.");
        }
        if (desde.plusDays(MAX_DIAS_RANGO).isBefore(hasta)) {
            throw new ReglaNegocioException("El rango del reporte no puede pasar de un año.");
        }

        Map<String, Map<EstadoCita, Long>> porEspecialidad = new LinkedHashMap<>();
        for (ConteoEstadoDTO fila : citaRepository.contarPorEspecialidadYEstado(desde, hasta)) {
            porEspecialidad.computeIfAbsent(fila.especialidad(), k -> new EnumMap<>(EstadoCita.class))
                    .merge(fila.estado(), fila.total(), Long::sum);
        }

        return porEspecialidad.entrySet().stream()
                .map(entrada -> {
                    Map<EstadoCita, Long> c = entrada.getValue();
                    long pendientes = c.getOrDefault(EstadoCita.PENDIENTE, 0L);
                    long confirmadas = c.getOrDefault(EstadoCita.CONFIRMADA, 0L);
                    long atendidas = c.getOrDefault(EstadoCita.ATENDIDA, 0L);
                    long noAsistio = c.getOrDefault(EstadoCita.NO_ASISTIO, 0L);
                    long canceladas = c.getOrDefault(EstadoCita.CANCELADA, 0L);
                    return new OcupacionDTO(entrada.getKey(), pendientes, confirmadas, atendidas,
                            noAsistio, canceladas, pendientes + confirmadas + atendidas + noAsistio + canceladas);
                })
                .toList();
    }
}
