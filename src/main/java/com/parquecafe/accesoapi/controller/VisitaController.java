package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.dto.RegistroVisitaRequest;
import com.parquecafe.accesoapi.model.RegistroVisita;
import com.parquecafe.accesoapi.repository.RegistroVisitaRepository;
import com.parquecafe.accesoapi.service.VisitaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitas")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://front-sistema-de-control-de-acceso.vercel.app"
})
// Pantalla de "Visitantes": pública, sin login (una de las 4 de portería).
public class VisitaController {

    private final VisitaService visitaService;
    private final RegistroVisitaRepository registroVisitaRepository;

    public VisitaController(VisitaService visitaService, RegistroVisitaRepository registroVisitaRepository) {
        this.visitaService = visitaService;
        this.registroVisitaRepository = registroVisitaRepository;
    }

    @GetMapping("/activas")
    public List<RegistroVisita> visitasActivas() {
        return registroVisitaRepository.findByFechaHoraSalidaIsNull();
    }

    @GetMapping
    public List<RegistroVisita> listarTodas() {
        return registroVisitaRepository.findAllByOrderByFechaHoraEntradaDesc();
    }

    @PostMapping("/ingreso")
    public RegistroVisita registrarIngreso(@RequestBody RegistroVisitaRequest request) {
        return visitaService.registrarIngreso(request);
    }

    @PostMapping("/{id}/salida")
    public RegistroVisita registrarSalida(@PathVariable Long id) {
        return visitaService.registrarSalida(id);
    }

    @GetMapping("/empleado/{empleadoId}")
    public List<RegistroVisita> historialPorEmpleado(@PathVariable Long empleadoId) {
        return visitaService.historialPorEmpleado(empleadoId);
    }

    @GetMapping("/empleado-directo/{empleadoDirectoId}")
    public List<RegistroVisita> historialPorEmpleadoDirecto(@PathVariable Long empleadoDirectoId) {
        return visitaService.historialPorEmpleadoDirecto(empleadoDirectoId);
    }

    @GetMapping("/visitante/{visitanteId}")
    public List<RegistroVisita> historialPorVisitante(@PathVariable Long visitanteId) {
        return visitaService.historialPorVisitante(visitanteId);
    }
}


