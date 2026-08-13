package com.parquecafe.accesoapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Guarda el estado de afiliación de un empleado para un mes/año específico.
 * Se guarda histórico (no se sobreescribe) para poder conservar meses
 * anteriores, tal como pide RF-13.
 *
 * IMPORTANTE: la Seguridad Social (SS) en Colombia cubre dos cosas distintas:
 * Salud (EPS) y Pensión (AFP). Por eso se manejan como dos campos separados,
 * cada uno con su propia entidad (nombre de la EPS / nombre de la AFP) y su
 * propia fecha de afiliación, en vez de un único booleano "afiliadoSS".
 */
@Entity
@Table(name = "afiliacion", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"empleado_id", "anio", "mes"})
})
@Getter
@Setter
@NoArgsConstructor
public class Afiliacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes; // 1 - 12

    // --- Salud ---
    @Column(nullable = false)
    private boolean afiliadoSalud;

    private String eps; // nombre de la EPS, ej. "Sanitas EPS"

    private LocalDate fechaAfiliacionSalud;

    // --- Pensión ---
    @Column(nullable = false)
    private boolean afiliadoPension;

    private String afp; // nombre de la AFP, ej. "Porvenir"

    private LocalDate fechaAfiliacionPension;


    // --- ARL ---
    @Column(name = "afiliado_arl", nullable = false)
    private boolean afiliadoARL;

    private String arl; // nombre de la ARL, ej. "ARL SURA"

    @Column(name = "fecha_afiliacion_arl")
    private LocalDate fechaAfiliacionARL;

    @Column(nullable = false)
    private LocalDateTime fechaCarga = LocalDateTime.now();
}
