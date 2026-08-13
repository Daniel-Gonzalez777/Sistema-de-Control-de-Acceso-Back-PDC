package com.parquecafe.accesoapi.dto;

import com.parquecafe.accesoapi.model.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Lo que ve el vigilante en pantalla tras consultar una cédula.
 * Es el mismo DTO que se usará cuando la cédula venga del escáner (CU-03),
 * porque la lógica de validación no cambia, solo la forma de captura.
 */
@Getter
@AllArgsConstructor
public class ResultadoValidacionDTO {

    private boolean autorizado;
    private String nombre;              // null si la cédula no fue encontrada
    private String concesionario;       // null si la cédula no fue encontrada
    private String motivo;              // explica por qué no está autorizado, o "OK" / "Salida registrada"
    private TipoMovimiento tipoMovimiento; // ENTRADA o SALIDA -- para que el front sepa qué mensaje mostrar
}
