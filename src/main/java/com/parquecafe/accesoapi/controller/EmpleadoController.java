package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.model.Empleado;
import com.parquecafe.accesoapi.repository.EmpleadoRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    // Lectura: pública. La usan las pantallas públicas de Visitantes e
    // Historial de visitas para sus selectores (a quién visita, filtro
    // por empresa), y también la pantalla de Empleados (solo Admin).
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

    // Crear/eliminar: exclusivo de Admin (ya no existe el rol Concesionario).
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Empleado crear(@Valid @RequestBody Empleado empleado) {
        return repository.save(empleado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizar(@PathVariable Long id, @Valid @RequestBody Empleado datos) {
        return repository.findById(id)
                .map(existente -> {
                    existente.setCedula(datos.getCedula());
                    existente.setNombre(datos.getNombre());
                    existente.setCargo(datos.getCargo());
                    existente.setArea(datos.getArea());
                    existente.setConcesionario(datos.getConcesionario());
                    existente.setActivo(datos.isActivo());
                    return ResponseEntity.ok(repository.save(existente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: este empleado ya tiene información asociada " +
                            "(afiliaciones, visitas o historial de ingresos). Márcalo como Inactivo en su lugar.");
        }
        return ResponseEntity.noContent().build();
    }
}
