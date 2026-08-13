package com.parquecafe.accesoapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Representa UNA fila de la hoja "Plantilla" del Excel, ya leída y
 * convertida a tipos de Java, pero todavía sin guardar en la base de datos.
 * El orden de los campos corresponde exactamente a las columnas del
 * Excel real que usan los concesionarios:
 *
 * cedula | nombre | cargo | area | concesionario_nit | afiliado_salud | eps |
 * fecha_afiliacion_salud | afiliado_pension | afp | fecha_afiliacion_pension |
 * afiliado_arl | arl | fecha_afiliacion_arl
 */
@Getter
@Setter
public class FilaExcelAfiliacion {

    private int numeroFila; // fila del Excel (para poder señalar errores exactos)

    private String cedula;
    private String nombre;
    private String cargo;
    private String area;
    private String concesionarioNit;

    private boolean afiliadoSalud;
    private String eps;
    private LocalDate fechaAfiliacionSalud;

    private boolean afiliadoPension;
    private String afp;
    private LocalDate fechaAfiliacionPension;

    private boolean afiliadoArl;
    private String arl;
    private LocalDate fechaAfiliacionArl;
}
