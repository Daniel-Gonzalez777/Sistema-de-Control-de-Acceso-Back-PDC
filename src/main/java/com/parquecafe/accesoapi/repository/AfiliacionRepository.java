package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.Afiliacion;
import com.parquecafe.accesoapi.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface
AfiliacionRepository extends JpaRepository<Afiliacion, Long> {

    Optional<Afiliacion> findByEmpleadoAndAnioAndMes(Empleado empleado, int anio, int mes);

    // Trae la afiliación más reciente que exista para el empleado,
    // usada por la regla RN-02/RN-03 (validar contra el último mes cargado).
    Optional<Afiliacion> findTopByEmpleadoOrderByAnioDescMesDesc(Empleado empleado);
}
