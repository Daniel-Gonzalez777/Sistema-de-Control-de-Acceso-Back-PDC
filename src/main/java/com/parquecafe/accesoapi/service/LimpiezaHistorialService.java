package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.repository.AfiliacionRepository;
import com.parquecafe.accesoapi.repository.RegistroIngresoEmpleadoRepository;
import com.parquecafe.accesoapi.repository.RegistroVisitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Limpieza automática del historial. Corre sola todos los días a las
 * 3:00 a.m. (hora del servidor) y limpia TRES cosas:
 *
 *   1) Ingresos/salidas de empleados (14 días) -- se conserva siempre el
 *      último movimiento de cada empleado, aunque tenga más de 14 días
 *      (para no perder el rastro de quién sigue dentro).
 *   2) Visitas (14 días) -- se conservan siempre las visitas que siguen
 *      ABIERTAS (sin hora de salida registrada), por la misma razón.
 *   3) Afiliaciones -- se conservan el mes actual y el mes anterior; solo
 *      se borra lo de 2 meses atrás o más. NO se puede limpiar por días
 *      como las otras dos, porque la validación de ingreso consulta la
 *      afiliación del mes en curso: borrarla dejaría a todos los empleados
 *      de esa empresa sin poder entrar por el resto del mes.
 */
@Service
public class LimpiezaHistorialService {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaHistorialService.class);
    private static final int DIAS_DE_RETENCION = 14;
    private static final int MESES_DE_AFILIACION_A_CONSERVAR = 2; // mes actual + mes anterior

    private final RegistroIngresoEmpleadoRepository registroRepository;
    private final RegistroVisitaRepository registroVisitaRepository;
    private final AfiliacionRepository afiliacionRepository;

    public LimpiezaHistorialService(RegistroIngresoEmpleadoRepository registroRepository,
                                    RegistroVisitaRepository registroVisitaRepository,
                                    AfiliacionRepository afiliacionRepository) {
        this.registroRepository = registroRepository;
        this.registroVisitaRepository = registroVisitaRepository;
        this.afiliacionRepository = afiliacionRepository;
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

        limpiarAfiliacionesAntiguas();
    }

    private void limpiarAfiliacionesAntiguas() {
        // Periodo corrido (anio * 12 + mes) del mes más antiguo que se conserva.
        // Con MESES_A_CONSERVAR = 2 y hoy = septiembre 2026 -> se conserva
        // agosto 2026 en adelante, y se borra julio 2026 y anteriores.
        YearMonth mesMasAntiguoQueSeConserva = YearMonth.now().minusMonths(MESES_DE_AFILIACION_A_CONSERVAR - 1L);
        int periodoLimite = mesMasAntiguoQueSeConserva.getYear() * 12 + mesMasAntiguoQueSeConserva.getMonthValue();

        int afiliacionesEliminadas = afiliacionRepository.eliminarAnterioresAlPeriodo(periodoLimite);
        log.info("Limpieza de afiliaciones: {} afiliación(es) anteriores a {} eliminada(s) (se conservan los últimos {} meses)",
                afiliacionesEliminadas, mesMasAntiguoQueSeConserva, MESES_DE_AFILIACION_A_CONSERVAR);
    }
}