package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.model.EmpleadoDirectoParque;
import com.parquecafe.accesoapi.repository.EmpleadoDirectoParqueRepository;
import com.parquecafe.accesoapi.service.VisitaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados-directos")
@CrossOrigin(origins = "http://localhost:4200")
public class EmpleadoDirectoParqueController {

    private final EmpleadoDirectoParqueRepository repository;

    public EmpleadoDirectoParqueController(EmpleadoDirectoParqueRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<EmpleadoDirectoParque> listar() {
        return repository.findAllByOrderByNombreAsc();
    }

    // Lista fija de áreas válidas, para que el frontend arme el <select>
    // exactamente con las mismas opciones que el backend va a validar.
    @GetMapping("/areas")
    public List<String> areasValidas() {
        return VisitaService.AREAS_EMPLEADO_DIRECTO;
    }
}