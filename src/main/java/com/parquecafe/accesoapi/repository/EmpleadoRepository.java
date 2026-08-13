package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByCedula(String cedula);
}
