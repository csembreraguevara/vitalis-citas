package com.clinicavitalis.citas.service.impl;

import com.clinicavitalis.citas.dto.PacienteRequestDTO;
import com.clinicavitalis.citas.dto.PacienteResumenDTO;
import com.clinicavitalis.citas.entity.Paciente;
import com.clinicavitalis.citas.exception.RecursoNoEncontradoException;
import com.clinicavitalis.citas.exception.ReglaNegocioException;
import com.clinicavitalis.citas.repository.PacienteRepository;
import com.clinicavitalis.citas.service.PacienteService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Registro de pacientes por recepción (RF004):
 * <ul>
 *   <li>El DNI no se repite (también lo protege el índice único uk_paciente_dni).</li>
 *   <li>La edad debe ser razonable (0 a 120 años).</li>
 *   <li>Los nombres se guardan sin espacios de más y con mayúscula inicial (Commons Lang).</li>
 * </ul>
 */
@Service
public class PacienteServiceImpl implements PacienteService {

    private static final Logger log = LoggerFactory.getLogger(PacienteServiceImpl.class);
    static final int EDAD_MAXIMA = 120;

    private final PacienteRepository pacienteRepository;
    private final Clock clock;

    public PacienteServiceImpl(PacienteRepository pacienteRepository, Clock clock) {
        this.pacienteRepository = pacienteRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResumenDTO buscarPorDni(String dni) {
        return pacienteRepository.findByDni(dni)
                .map(PacienteResumenDTO::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay un paciente registrado con ese DNI"));
    }

    @Override
    @Transactional
    public PacienteResumenDTO registrar(PacienteRequestDTO solicitud, String registradoPor) {
        if (pacienteRepository.findByDni(solicitud.dni()).isPresent()) {
            throw new ReglaNegocioException("Ya existe un paciente registrado con el DNI " + solicitud.dni() + ".");
        }
        LocalDate hoy = LocalDate.now(clock);
        if (solicitud.fechaNacimiento().isBefore(hoy.minusYears(EDAD_MAXIMA))) {
            throw new ReglaNegocioException("Revisa la fecha de nacimiento.");
        }

        Paciente paciente = new Paciente(solicitud.dni(), nombrePropio(solicitud.nombres()),
                nombrePropio(solicitud.apellidos()), solicitud.fechaNacimiento(), solicitud.sexo(),
                StringUtils.lowerCase(StringUtils.trim(solicitud.email())));
        paciente.setTelefono(StringUtils.trimToNull(solicitud.telefono()));
        paciente.setDireccion(StringUtils.trimToNull(StringUtils.normalizeSpace(solicitud.direccion())));

        try {
            pacienteRepository.saveAndFlush(paciente);
        } catch (DataIntegrityViolationException ex) {
            // Otra persona registró el mismo DNI al mismo tiempo (índice único).
            throw new ReglaNegocioException("Ya existe un paciente registrado con el DNI " + solicitud.dni() + ".");
        }
        log.info("Paciente {} registrado por {}", paciente.getId(), registradoPor);
        return PacienteResumenDTO.de(paciente);
    }

    /** "  maría   del CARMEN " -> "María Del Carmen" */
    private static String nombrePropio(String texto) {
        String limpio = StringUtils.lowerCase(StringUtils.normalizeSpace(texto), Locale.forLanguageTag("es-PE"));
        return Arrays.stream(StringUtils.split(limpio, ' '))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }
}
