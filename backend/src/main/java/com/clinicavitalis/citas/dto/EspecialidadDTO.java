package com.clinicavitalis.citas.dto;

import com.clinicavitalis.citas.entity.Especialidad;

/** Especialidad que se muestra en el catálogo público (RF005). */
public record EspecialidadDTO(Long id, String nombre, String descripcion, int duracionCitaMin) {

    public static EspecialidadDTO de(Especialidad especialidad) {
        return new EspecialidadDTO(especialidad.getId(), especialidad.getNombre(),
                especialidad.getDescripcion(), especialidad.getDuracionCitaMin());
    }
}
