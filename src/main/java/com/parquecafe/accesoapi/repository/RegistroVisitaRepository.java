package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.RegistroVisita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroVisitaRepository extends JpaRepository<RegistroVisita, Long> {

    // Visitas que aún no tienen hora de salida registrada (visitantes que siguen dentro del Parque)
    List<RegistroVisita> findByFechaHoraSalidaIsNull();

    // Historial de visitas recibidas por un empleado del sistema (el más reciente primero)
    List<RegistroVisita> findByEmpleadoVisitadoIdOrderByFechaHoraEntradaDesc(Long empleadoId);

    // Historial de visitas recibidas por un empleado directo del parque
    List<RegistroVisita> findByEmpleadoDirectoIdOrderByFechaHoraEntradaDesc(Long empleadoDirectoId);

    // Historial de visitas hechas por un visitante en particular
    List<RegistroVisita> findByVisitanteIdOrderByFechaHoraEntradaDesc(Long visitanteId);

    // Limpieza automática: borra visitas ya CERRADAS (con salida registrada) de más
    // de 14 días. Nunca borra una visita que sigue abierta (fechaHoraSalida = null),
    // para no perder el rastro de quién sigue dentro del Parque.
    @Modifying
    @Transactional
    @Query("DELETE FROM RegistroVisita r WHERE r.fechaHoraSalida IS NOT NULL AND r.fechaHoraEntrada < :fecha")
    int eliminarVisitasCerradasAnterioresA(@Param("fecha") LocalDateTime fecha);
}