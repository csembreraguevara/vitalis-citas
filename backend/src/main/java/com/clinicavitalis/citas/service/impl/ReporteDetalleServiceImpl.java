package com.clinicavitalis.citas.service.impl;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.dto.ConteoMedicoEstadoDTO;
import com.clinicavitalis.citas.dto.OcupacionMedicoDTO;
import com.clinicavitalis.citas.entity.EstadoCita;
import com.clinicavitalis.citas.exception.ReglaNegocioException;
import com.clinicavitalis.citas.repository.ReporteRepository;
import com.clinicavitalis.citas.service.ReporteDetalleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReporteDetalleServiceImpl implements ReporteDetalleService {

    private final ReporteRepository reporteRepository;

    public ReporteDetalleServiceImpl(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Override
    public List<OcupacionMedicoDTO> ocupacionPorMedico(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);

        // Agrupa las filas (médico, estado, total) en una fila por médico.
        Map<Long, ConteoMedicoEstadoDTO> datosMedico = new LinkedHashMap<>();
        Map<Long, Map<EstadoCita, Long>> conteos = new LinkedHashMap<>();
        for (ConteoMedicoEstadoDTO fila : reporteRepository.contarPorMedicoYEstado(desde, hasta)) {
            datosMedico.putIfAbsent(fila.idMedico(), fila);
            conteos.computeIfAbsent(fila.idMedico(), k -> new EnumMap<>(EstadoCita.class))
                    .merge(fila.estado(), fila.total(), Long::sum);
        }

        return datosMedico.values().stream()
                .map(medico -> {
                    Map<EstadoCita, Long> c = conteos.get(medico.idMedico());
                    long pendientes = c.getOrDefault(EstadoCita.PENDIENTE, 0L);
                    long confirmadas = c.getOrDefault(EstadoCita.CONFIRMADA, 0L);
                    long atendidas = c.getOrDefault(EstadoCita.ATENDIDA, 0L);
                    long noAsistio = c.getOrDefault(EstadoCita.NO_ASISTIO, 0L);
                    long canceladas = c.getOrDefault(EstadoCita.CANCELADA, 0L);
                    return new OcupacionMedicoDTO(medico.idMedico(),
                            medico.nombres() + " " + medico.apellidos(), medico.especialidad(),
                            pendientes, confirmadas, atendidas, noAsistio, canceladas,
                            pendientes + confirmadas + atendidas + noAsistio + canceladas);
                })
                .toList();
    }

    @Override
    public List<CitaDTO> citasDelMedico(Long idMedico, LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        return reporteRepository.citasDelMedico(idMedico, desde, hasta).stream()
                .map(CitaDTO::de)
                .toList();
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la inicial.");
        }
        if (desde.plusDays(ReporteServiceImpl.MAX_DIAS_RANGO).isBefore(hasta)) {
            throw new ReglaNegocioException("El rango del reporte no puede pasar de un año.");
        }
    }
}
