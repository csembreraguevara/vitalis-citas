package com.clinicavitalis.citas.repository;

import com.clinicavitalis.citas.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** DAO de la tabla {@code especialidad}. */
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    List<Especialidad> findByActivoTrueOrderByNombreAsc();
}
