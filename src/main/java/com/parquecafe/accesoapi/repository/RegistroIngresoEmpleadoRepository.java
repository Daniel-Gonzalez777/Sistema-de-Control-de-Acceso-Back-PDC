package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.Empleado;
import com.parquecafe.accesoapi.model.RegistroIngresoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistroIngresoEmpleadoRepository extends JpaRepository<RegistroIngresoEmpleado, Long> {

    // El movimiento más reciente de un empleado, para saber si está
    // actualmente dentro (último movimiento = ENTRADA autorizada) o fuera.
    Optional<RegistroIngresoEmpleado> findTopByEmpleadoOrderByFechaHoraDesc(Empleado empleado);

    // Historial completo, del más reciente al más antiguo.
    List<RegistroIngresoEmpleado> findAllByOrderByFechaHoraDesc();

    // Todos los movimientos de los empleados de UN concesionario, dentro de un
    // rango de fechas (usado para armar el calendario mensual). Nunca trae
    // registros de otros concesionarios, porque filtra por el concesionario
    // del empleado dueño del registro.
    List<RegistroIngresoEmpleado> findByEmpleado_Concesionario_IdAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long concesionarioId, LocalDateTime desde, LocalDateTime hasta);
}
