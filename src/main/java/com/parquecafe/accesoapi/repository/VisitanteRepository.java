package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitanteRepository extends JpaRepository<Visitante, Long> {

    Optional<Visitante> findByDocumento(String documento);
}