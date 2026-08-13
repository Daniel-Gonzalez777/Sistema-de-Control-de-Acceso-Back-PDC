package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.dto.RegistroVisitaRequest;
import com.parquecafe.accesoapi.model.RegistroVisita;
import com.parquecafe.accesoapi.repository.RegistroVisitaRepository;
import com.parquecafe.accesoapi.service.VisitaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitas")
@CrossOrigin(origins = "http://localhost:4200")
public class VisitaController {

    private final VisitaService visitaService;
    private final RegistroVisitaRepository registroVisitaRepository;

    public VisitaController(VisitaService visitaService, RegistroVisitaRepository registroVisitaRepository) {
        this.visitaService = visitaService;
        this.registroVisitaRepository = registroVisitaRepository;
    }

    // Visitantes que actualmente siguen dentro del Parque (sin salida registrada)
    @GetMapping("/activas")
    public List<RegistroVisita> visitasActivas() {
        return registroVisitaRepository.findByFechaHoraSalidaIsNull();
    }

    @GetMapping
    public List<RegistroVisita> listarTodas() {
        return registroVisitaRepository.findAll();
    }

    @PostMapping("/ingreso")
    public RegistroVisita registrarIngreso(@RequestBody RegistroVisitaRequest request) {
        return visitaService.registrarIngreso(request);
    }

    @PostMapping("/{id}/salida")
    public RegistroVisita registrarSalida(@PathVariable Long id) {
        return visitaService.registrarSalida(id);
    }
}
