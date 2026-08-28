package com.parquecafe.accesoapi.dto;

import com.parquecafe.accesoapi.model.ResultadoIngreso;
import com.parquecafe.accesoapi.model.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

/**
 * Un movimiento individual (una fila) dentro del calendario de un día.
 */
@Getter
@AllArgsConstructor
public class MovimientoCalendarioDTO {

    private String nombreEmpleado;  // null si la cédula no correspondía a nadie registrado
    private String cedula;
    private LocalTime hora;
    private TipoMovimiento tipoMovimiento;
    private ResultadoIngreso resultado;
    private String motivo;
}