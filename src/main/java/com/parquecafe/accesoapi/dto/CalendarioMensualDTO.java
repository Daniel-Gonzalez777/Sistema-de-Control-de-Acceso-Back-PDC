package com.parquecafe.accesoapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Calendario mensual completo de UN concesionario: nunca mezcla información
 * de otro concesionario, porque se arma a partir de una consulta filtrada
 * por concesionarioId (ver IngresoService.obtenerCalendarioMensual).
 */
@Getter
@AllArgsConstructor
public class CalendarioMensualDTO {

    private Long concesionarioId;
    private String concesionarioNombre;
    private int anio;
    private int mes;
    private List<DiaCalendarioDTO> dias; // solo los días que tuvieron al menos un movimiento
}