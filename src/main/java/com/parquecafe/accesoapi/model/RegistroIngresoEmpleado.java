package com.parquecafe.accesoapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Deja trazabilidad de CADA movimiento (entrada o salida), autorizado o no (RN-05).
 * Es también el historial: para saber quién está actualmente dentro del Parque,
 * basta con mirar cuál fue el último movimiento AUTORIZADO de cada empleado.
 */
@Entity
@Table(name = "registro_ingreso_empleado")
@Getter
@Setter
@NoArgsConstructor
public class RegistroIngresoEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cedulaConsultada;

    // Puede ser null si la cédula no correspondía a ningún empleado registrado
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoIngreso resultado;

    // ENTRADA o SALIDA. Los registros viejos (antes de este cambio) se migran como ENTRADA.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipoMovimiento = TipoMovimiento.ENTRADA;

    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();
}
