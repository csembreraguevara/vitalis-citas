package com.clinicavitalis.citas.repository;

import com.clinicavitalis.citas.entity.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** DAO de la tabla {@code horario_medico}. */
public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Long> {

    /** Bloques activos de un médico en un día (1 = lunes … 7 = domingo). */
    List<HorarioMedico> findByMedicoIdAndDiaSemanaAndActivoTrueOrderByHoraInicioAsc(Long idMedico, int diaSemana);
}
