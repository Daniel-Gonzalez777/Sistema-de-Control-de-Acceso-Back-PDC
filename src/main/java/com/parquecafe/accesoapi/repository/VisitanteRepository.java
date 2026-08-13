package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitanteRepository extends JpaRepository<Visitante, Long> {
}
