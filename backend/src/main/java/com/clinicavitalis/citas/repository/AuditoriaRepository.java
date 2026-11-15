package com.clinicavitalis.citas.repository;

import com.clinicavitalis.citas.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

/** DAO de la tabla {@code auditoria}. */
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
}
