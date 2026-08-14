package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.EmpleadoDirectoParque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpleadoDirectoParqueRepository extends JpaRepository<EmpleadoDirectoParque, Long> {

    Optional<EmpleadoDirectoParque> findByCedula(String cedula);

    List<EmpleadoDirectoParque> findAllByOrderByNombreAsc();
}