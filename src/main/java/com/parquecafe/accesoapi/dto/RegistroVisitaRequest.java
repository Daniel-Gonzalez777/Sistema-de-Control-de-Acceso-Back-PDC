package com.parquecafe.accesoapi.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Lo que envía el formulario de registro de visitante (CU-04).
 *
 * Para el empleado a visitar, el frontend manda UNA de estas dos cosas:
 *  - empleadoVisitadoId: si eligió un empleado ya existente en el sistema.
 *  - empleadoDirectoCedula / empleadoDirectoNombre / empleadoDirectoArea:
 *    si es un empleado directo del parque que no está en la tabla `empleado`.
 */
@Getter
@Setter
public class RegistroVisitaRequest {

    private String nombreVisitante;
    private String documentoVisitante;

    private Long empleadoVisitadoId;

    private String empleadoDirectoCedula;
    private String empleadoDirectoNombre;
    private String empleadoDirectoArea;

    private String motivo;

    private boolean ingresaVehiculo;
    private String placaVehiculo;
    private String tipoVehiculo;
    private String zonaParqueo;
}