package com.evaluacion.usuarios.repository;

import com.evaluacion.usuarios.model.Telefono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface TelefonoRepository extends JpaRepository<Telefono, Long> {
    List<Telefono> findByUsuarioId(UUID usuarioId);
}
