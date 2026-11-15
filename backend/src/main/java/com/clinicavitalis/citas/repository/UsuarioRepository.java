package com.clinicavitalis.citas.repository;

import com.clinicavitalis.citas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** DAO de la tabla {@code usuario}. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
}
