package com.clinicavitalis.citas.repository;

import com.clinicavitalis.citas.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** DAO de la tabla {@code medico}. Las consultas traen la especialidad con JOIN FETCH. */
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    /** Médicos activos; si {@code idEspecialidad} es null, de todas las especialidades. */
    @Query("""
            select m from Medico m join fetch m.especialidad e
            where m.activo = true and e.activo = true
              and (:idEspecialidad is null or e.id = :idEspecialidad)
            order by m.apellidos, m.nombres
            """)
    List<Medico> buscarActivos(@Param("idEspecialidad") Long idEspecialidad);

    @Query("select m from Medico m join fetch m.especialidad where m.id = :id")
    Optional<Medico> buscarConEspecialidad(@Param("id") Long id);
}
