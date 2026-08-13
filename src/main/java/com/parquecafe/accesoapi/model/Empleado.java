package com.parquecafe.accesoapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "empleado")
@Getter
@Setter
@NoArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Este es el campo clave: es lo que se digita hoy y lo que
    // recibirá el lector/escáner de cédula más adelante (RF-12 / RNF-04).
    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    private String cargo;

    private String area;

    @ManyToOne(optional = false)
    @JoinColumn(name = "concesionario_id", nullable = false)
    private Concesionario concesionario;
}
