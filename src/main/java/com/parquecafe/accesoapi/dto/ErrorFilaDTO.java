package com.parquecafe.accesoapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Un problema encontrado en UNA fila específica del Excel, para que quien
 * subió el archivo sepa exactamente qué corregir (y no tenga que adivinar).
 */
@Getter
@AllArgsConstructor
public class ErrorFilaDTO {
    private int fila;       // número de fila en el Excel, ej: 5
    private String cedula;  // cédula de esa fila (puede venir null/vacía si el error es justamente que falta)
    private String mensaje; // explicación legible del problema
}
