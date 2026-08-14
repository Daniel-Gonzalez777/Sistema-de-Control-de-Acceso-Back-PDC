package com.parquecafe.accesoapi.repository;

import com.parquecafe.accesoapi.model.RegistroVisita;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
