package com.parquecafe.accesoapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "concesionario")
@Getter
@Setter
@NoArgsConstructor
public class Concesionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Column(unique = true)
    private String nit;

    // RN-04: si un concesionario está inactivo, ninguno de sus empleados
    // puede quedar autorizado, sin importar su afiliación.
    @Column(nullable = false)
    private boolean activo = true;
}
