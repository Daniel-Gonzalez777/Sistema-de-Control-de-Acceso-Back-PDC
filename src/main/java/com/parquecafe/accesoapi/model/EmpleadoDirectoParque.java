package com.parquecafe.accesoapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Empleados directos del Parque del Café.
// que NO pertenecen a un concesionario y por eso no pasan por el registro
// completo de la tabla `empleado`. Se guardan aqui solo lo minimo necesario
// para poder llevar su historial de visitas recibidas.
@Entity
@Table(name = "empleado_directo_parque")
@Getter
@Setter
@NoArgsConstructor
public class EmpleadoDirectoParque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String area;
}