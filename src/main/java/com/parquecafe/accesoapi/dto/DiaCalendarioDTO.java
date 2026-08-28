package com.parquecafe.accesoapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Un día del calendario mensual, con todos los movimientos (entradas/salidas)
 * de los empleados de UN SOLO concesionario ese día.
 */
@Getter
@AllArgsConstructor
public class DiaCalendarioDTO {

    private int dia; // 1 - 31
    private List<MovimientoCalendarioDTO> movimientos;
}