package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.model.Empleado;
import com.parquecafe.accesoapi.repository.EmpleadoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://front-sistema-de-control-de-acceso.vercel.app"
})
public class EmpleadoController {

    private final EmpleadoRepository repository;

    public EmpleadoController(EmpleadoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Empleado> listar(@RequestParam(required = false) Long concesionarioId) {
        if (concesionarioId != null) {
            return repository.findByConcesionarioId(concesionarioId);
        }
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Empleado crear(@Valid @RequestBody Empleado empleado) {
        return repository.save(empleado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}