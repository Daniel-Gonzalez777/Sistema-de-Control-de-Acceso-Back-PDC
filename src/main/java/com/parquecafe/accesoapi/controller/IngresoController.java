package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.dto.CalendarioMensualDTO;
import com.parquecafe.accesoapi.dto.ResultadoValidacionDTO;
import com.parquecafe.accesoapi.model.RegistroIngresoEmpleado;
import com.parquecafe.accesoapi.service.CalendarioExcelService;
import com.parquecafe.accesoapi.service.IngresoService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/ingreso")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://front-sistema-de-control-de-acceso.vercel.app"
})
public class IngresoController {

    private final IngresoService ingresoService;
    private final CalendarioExcelService calendarioExcelService;

    public IngresoController(IngresoService ingresoService, CalendarioExcelService calendarioExcelService) {
        this.ingresoService = ingresoService;
        this.calendarioExcelService = calendarioExcelService;
    }

    // Público: pantalla de portería, sin login.
    @GetMapping("/validar/{cedula}")
    public ResultadoValidacionDTO validar(@PathVariable String cedula) {
        return ingresoService.validarIngreso(cedula);
    }

    // Público: pantalla de Historial, sin login.
    @GetMapping("/historial")
    public List<RegistroIngresoEmpleado> historial() {
        return ingresoService.obtenerHistorial();
    }

    // Público: mismo criterio que el historial.
    @GetMapping("/dentro")
    public List<RegistroIngresoEmpleado> quienesEstanDentro() {
        return ingresoService.obtenerEmpleadosDentro();
    }

    // Calendario: exclusivo de Admin (ya no existe el rol Concesionario).
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/calendario")
    public CalendarioMensualDTO calendario(
            @RequestParam Long concesionarioId,
            @RequestParam int anio,
            @RequestParam int mes) {
        return ingresoService.obtenerCalendarioMensual(concesionarioId, anio, mes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/calendario/exportar")
    public ResponseEntity<ByteArrayResource> exportarCalendario(
            @RequestParam Long concesionarioId,
            @RequestParam int anio,
            @RequestParam int mes) throws java.io.IOException {

        CalendarioMensualDTO calendario = ingresoService.obtenerCalendarioMensual(concesionarioId, anio, mes);
        ByteArrayOutputStream excel = calendarioExcelService.generarExcel(calendario);

        String nombreArchivo = "calendario_" + calendario.getConcesionarioNombre().replaceAll("\\s+", "_")
                + "_" + anio + "_" + String.format("%02d", mes) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(excel.toByteArray()));
    }
}
