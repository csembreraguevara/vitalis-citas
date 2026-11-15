package com.clinicavitalis.citas.repository;

import com.clinicavitalis.citas.dto.ConteoMedicoEstadoDTO;
import com.clinicavitalis.citas.entity.Cita;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/** Consultas JPQL de solo lectura para el detalle de los reportes. */
public interface ReporteRepository extends Repository<Cita, Long> {

    @Query("""
            select new com.clinicavitalis.citas.dto.ConteoMedicoEstadoDTO(
                   m.id, m.nombres, m.apellidos, e.nombre, c.estado, count(c))
            from Cita c join c.medico m join m.especialidad e
            where c.fecha between :desde and :hasta
            group by m.id, m.nombres, m.apellidos, e.nombre, c.estado
            order by e.nombre, m.apellidos, m.nombres
            """)
    List<ConteoMedicoEstadoDTO> contarPorMedicoYEstado(@Param("desde") LocalDate desde,
                                                       @Param("hasta") LocalDate hasta);

    /** Citas del médico en el periodo; las reservas que expiraron no se cuentan (igual que el reporte). */
    @Query("""
            select c from Cita c
            join fetch c.medico m join fetch m.especialidad
            join fetch c.paciente
            where m.id = :idMedico and c.fecha between :desde and :hasta
              and c.estado <> com.clinicavitalis.citas.entity.EstadoCita.EXPIRADA
            order by c.fecha, c.hora
            """)
    List<Cita> citasDelMedico(@Param("idMedico") Long idMedico,
                              @Param("desde") LocalDate desde,
                              @Param("hasta") LocalDate hasta);

    /** Todas las citas del periodo (sin expiradas) para la hoja de detalle del Excel. */
    @Query("""
            select c from Cita c
            join fetch c.medico m join fetch m.especialidad e
            join fetch c.paciente
            where c.fecha between :desde and :hasta
              and c.estado <> com.clinicavitalis.citas.entity.EstadoCita.EXPIRADA
            order by c.fecha, c.hora, e.nombre
            """)
    List<Cita> citasDelPeriodo(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
