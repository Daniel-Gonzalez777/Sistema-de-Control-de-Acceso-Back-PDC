package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.repository.RegistroIngresoEmpleadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Limpieza automática del historial de ingresos/salidas: retención de 14 días.
 *
 * Corre sola todos los días a las 3:00 a.m. (hora del servidor). Borra los
 * movimientos con más de 14 días de antigüedad, PERO nunca borra el último
 * movimiento de un empleado -- eso evita perder el rastro de alguien que
 * entró hace más de 2 semanas y todavía no ha registrado su salida.
 */
@Service
public class LimpiezaHistorialService {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaHistorialService.class);
    private static final int DIAS_DE_RETENCION = 14;

    private final RegistroIngresoEmpleadoRepository registroRepository;

    public LimpiezaHistorialService(RegistroIngresoEmpleadoRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    // cron: segundo minuto hora día-mes mes día-semana -> "0 0 3 * * *" = todos los días a las 3:00:00 a.m.
    @Scheduled(cron = "0 0 3 * * *")
    public void limpiarHistorialAntiguo() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(DIAS_DE_RETENCION);
        int eliminados = registroRepository.eliminarAnterioresAConservandoUltimoPorEmpleado(fechaLimite);
        log.info("Limpieza de historial: {} registro(s) con más de {} días eliminado(s) (se conservó el último movimiento de cada empleado)",
                eliminados, DIAS_DE_RETENCION);
    }
}