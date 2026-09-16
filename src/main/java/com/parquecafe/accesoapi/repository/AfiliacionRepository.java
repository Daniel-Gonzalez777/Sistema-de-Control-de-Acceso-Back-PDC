package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.Afiliacion;
import com.parquecafe.accesoapi.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface
AfiliacionRepository extends JpaRepository<Afiliacion, Long> {

    Optional<Afiliacion> findByEmpleadoAndAnioAndMes(Empleado empleado, int anio, int mes);

    // Trae la afiliación más reciente que exista para el empleado,
    // usada por la regla RN-02/RN-03 (validar contra el último mes cargado).
    Optional<Afiliacion> findTopByEmpleadoOrderByAnioDescMesDesc(Empleado empleado);

    // Listado para la pantalla de Afiliaciones: lo más reciente primero
    // (por periodo, y dentro del mismo periodo por fecha de carga).
    List<Afiliacion> findAllByOrderByAnioDescMesDescFechaCargaDesc();

    // Limpieza automática: borra las afiliaciones de periodos anteriores al
    // límite indicado. Se conservan SIEMPRE el mes actual y el mes anterior,
    // porque la validación de ingreso consulta la afiliación del mes en curso
    // -- borrar eso rompería el acceso de todos los empleados de esa empresa.
    // El cálculo (anio * 12 + mes) convierte el periodo a un número corrido,
    // para poder compararlo de una sola vez sin enredarse con el cambio de año.
    @Modifying
    @Transactional
    @Query("DELETE FROM Afiliacion a WHERE (a.anio * 12 + a.mes) < :periodoLimite")
    int eliminarAnterioresAlPeriodo(@Param("periodoLimite") int periodoLimite);
}