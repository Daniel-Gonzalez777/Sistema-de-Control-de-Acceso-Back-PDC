package com.parquecafe.accesoapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_visita")
@Getter
@Setter
@NoArgsConstructor
public class RegistroVisita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "visitante_id", nullable = false)
    private Visitante visitante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "empleado_visitado_id", nullable = false)
    private Empleado empleadoVisitado;

    // Área donde trabaja el empleado visitado, tomada al momento del registro
    private String area;

    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fechaHoraEntrada = LocalDateTime.now();

    private LocalDateTime fechaHoraSalida;

    private boolean ingresaVehiculo;

    private String placaVehiculo;

    private String tipoVehiculo;

    private String zonaParqueo;
}
