package com.parquecafe.accesoapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Lo que ve el concesionario (o el administrador) después de subir el Excel:
 * un resumen claro de qué pasó, fila por fila si hubo problemas.
 */
@Getter
@AllArgsConstructor
public class ResultadoCargaDTO {
    private int totalFilas;
    private int filasExitosas;
    private int filasConError;
    private int empleadosCreados;
    private int empleadosActualizados;
    private List<ErrorFilaDTO> errores;
}
