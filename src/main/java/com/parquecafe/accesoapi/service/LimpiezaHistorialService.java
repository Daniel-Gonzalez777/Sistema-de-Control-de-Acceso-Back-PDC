package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.repository.RegistroIngresoEmpleadoRepository;
import com.parquecafe.accesoapi.repository.RegistroVisitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Limpieza automática del historial: retención de 14 días.
 *
 * Corre sola todos los días a las 3:00 a.m. (hora del servidor) y limpia DOS
 * historiales:
 *   1) Ingresos/salidas de empleados -- se conserva siempre el último
 *      movimiento de cada empleado, aunque tenga más de 14 días (para no
 *      perder el rastro de quién sigue dentro).
 *   2) Visitas -- se conservan siempre las visitas que siguen ABIERTAS
 *      (sin hora de salida registrada), por la misma razón.
 */
@Service
public class LimpiezaHistorialService {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaHistorialService.class);
    private static final int DIAS_DE_RETENCION = 14;

    private final RegistroIngresoEmpleadoRepository registroRepository;
    private final RegistroVisitaRepository registroVisitaRepository;

    public LimpiezaHistorialService(RegistroIngresoEmpleadoRepository registroRepository,
                                    RegistroVisitaRepository registroVisitaRepository) {
        this.registroRepository = registroRepository;
        this.registroVisitaRepository = registroVisitaRepository;
    }

    // cron: segundo minuto hora día-mes mes día-semana -> "0 0 3 * * *" = todos los días a las 3:00:00 a.m.
    @Scheduled(cron = "0 0 3 * * *")
    public void limpiarHistorialAntiguo() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(DIAS_DE_RETENCION);

        int ingresosEliminados = registroRepository.eliminarAnterioresAConservandoUltimoPorEmpleado(fechaLimite);
        log.info("Limpieza de historial de ingresos: {} registro(s) con más de {} días eliminado(s) (se conservó el último movimiento de cada empleado)",
                ingresosEliminados, DIAS_DE_RETENCION);

        int visitasEliminadas = registroVisitaRepository.eliminarVisitasCerradasAnterioresA(fechaLimite);
        log.info("Limpieza de historial de visitas: {} visita(s) cerrada(s) con más de {} días eliminada(s) (se conservaron las visitas todavía abiertas)",
                visitasEliminadas, DIAS_DE_RETENCION);
    }
}