package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.Concesionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConcesionarioRepository extends JpaRepository<Concesionario, Long> {

    Optional<Concesionario> findByNit(String nit);
}
