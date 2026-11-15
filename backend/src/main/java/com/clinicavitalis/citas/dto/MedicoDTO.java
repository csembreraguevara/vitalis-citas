package com.clinicavitalis.citas.dto;

import com.clinicavitalis.citas.entity.Medico;

/** Médico que se muestra en el catálogo público (RF006). No expone datos de contacto. */
public record MedicoDTO(Long id, String nombreCompleto, Long idEspecialidad, String especialidad,
                        String consultorio) {

    /** Requiere que la especialidad venga cargada (JOIN FETCH en el repositorio). */
    public static MedicoDTO de(Medico medico) {
        return new MedicoDTO(medico.getId(), medico.getNombreCompleto(),
                medico.getEspecialidad().getId(), medico.getEspecialidad().getNombre(),
                medico.getConsultorio());
    }
}
