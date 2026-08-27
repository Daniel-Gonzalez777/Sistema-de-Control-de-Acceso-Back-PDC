package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.dto.ResultadoValidacionDTO;
import com.parquecafe.accesoapi.model.RegistroIngresoEmpleado;
import com.parquecafe.accesoapi.service.IngresoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingreso")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://sistema-de-control-de-acceso-back-pdc.onrender.com/"
}) // origen por defecto de Angular en desarrollo
public class IngresoController {

    private final IngresoService ingresoService;

    public IngresoController(IngresoService ingresoService) {
        this.ingresoService = ingresoService;
    }

    // GET /api/ingreso/validar/1002345678
    // Si la persona no está dentro, valida y registra su ENTRADA.
    // Si ya está dentro, este mismo llamado registra su SALIDA.
    @GetMapping("/validar/{cedula}")
    public ResultadoValidacionDTO validar(@PathVariable String cedula) {
        return ingresoService.validarIngreso(cedula);
    }

    // Historial completo de movimientos (entradas y salidas, autorizados o no),
    // del más reciente al más antiguo.
    @GetMapping("/historial")
    public List<RegistroIngresoEmpleado> historial() {
        return ingresoService.obtenerHistorial();
    }

    // Quiénes están AHORA MISMO dentro del Parque (entraron y no han salido).
    @GetMapping("/dentro")
    public List<RegistroIngresoEmpleado> quienesEstanDentro() {
        return ingresoService.obtenerEmpleadosDentro();
    }
}
