package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.Empleado;
import com.parquecafe.accesoapi.model.RegistroIngresoEmpleado;
import com.parquecafe.accesoapi.model.ResultadoIngreso;
import com.parquecafe.accesoapi.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistroIngresoEmpleadoRepository extends JpaRepository<RegistroIngresoEmpleado, Long> {

    // El movimiento más reciente de un empleado, para saber si está
    // actualmente dentro (último movimiento = ENTRADA autorizada) o fuera.
    Optional<RegistroIngresoEmpleado> findTopByEmpleadoOrderByFechaHoraDesc(Empleado empleado);

    // La última entrada AUTORIZADA de un empleado (ignorando intentos
    // repetidos de "ya ingresó" que quedan guardados después, para no
    // perder de vista la entrada real del día). Se usa para decidir si
    // ya entró hoy, sin importar cuántas veces haya vuelto a escanear.
    Optional<RegistroIngresoEmpleado> findTopByEmpleadoAndTipoMovimientoAndResultadoOrderByFechaHoraDesc(
            Empleado empleado, TipoMovimiento tipoMovimiento, ResultadoIngreso resultado);

    // Historial completo, del más reciente al más antiguo.
    List<RegistroIngresoEmpleado> findAllByOrderByFechaHoraDesc();

    // Todos los movimientos de los empleados de UN concesionario, dentro de un
    // rango de fechas (usado para armar el calendario mensual). Nunca trae
    // registros de otros concesionarios, porque filtra por el concesionario
    // del empleado dueño del registro.
    List<RegistroIngresoEmpleado> findByEmpleado_Concesionario_IdAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long concesionarioId, LocalDateTime desde, LocalDateTime hasta);

    // Todas las entradas AUTORIZADAS dentro de un rango de fechas (se usa
    // con "hoy 00:00" a "hoy 23:59" para saber quién ya entró hoy). Como
    // solo puede existir UNA entrada autorizada por empleado por día, esta
    // consulta ya da directamente "quién está dentro", sin tener que
    // adivinar cuál de varios registros del mismo empleado es el válido.
    List<RegistroIngresoEmpleado> findByTipoMovimientoAndResultadoAndFechaHoraBetween(
            TipoMovimiento tipoMovimiento, ResultadoIngreso resultado, LocalDateTime desde, LocalDateTime hasta);

    // Limpieza automática del historial (RF de retención de datos): borra
    // todo lo anterior a "fecha", EXCEPTO el último movimiento de cada
    // empleado -- así nunca se pierde el rastro de quién sigue actualmente
    // dentro del Parque, aunque su entrada tenga más de 2 semanas.
    @Modifying
    @Transactional
    @Query("DELETE FROM RegistroIngresoEmpleado r WHERE r.fechaHora < :fecha " +
            "AND r.id NOT IN (" +
            "   SELECT MAX(r2.id) FROM RegistroIngresoEmpleado r2 " +
            "   WHERE r2.empleado IS NOT NULL GROUP BY r2.empleado" +
            ")")
    int eliminarAnterioresAConservandoUltimoPorEmpleado(@Param("fecha") LocalDateTime fecha);
}