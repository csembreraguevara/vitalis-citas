package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.EspecialidadDTO;
import com.clinicavitalis.citas.dto.MedicoDTO;

import java.util.List;

/** Catálogo público de especialidades y médicos (RF005, RF006). */
public interface CatalogoService {

    List<EspecialidadDTO> listarEspecialidades();

    /** Médicos activos; con {@code idEspecialidad} null devuelve todos. */
    List<MedicoDTO> listarMedicos(Long idEspecialidad);
}
