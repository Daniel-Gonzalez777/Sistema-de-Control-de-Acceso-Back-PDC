package com.parquecafe.accesoapi.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Lo que envía el formulario de registro de visitante (CU-04).
 */
@Getter
@Setter
public class RegistroVisitaRequest {

    private String nombreVisitante;
    private String documentoVisitante;
    private Long empleadoVisitadoId;
    private String motivo;

    private boolean ingresaVehiculo;
    private String placaVehiculo;
    private String tipoVehiculo;
    private String zonaParqueo;
}
