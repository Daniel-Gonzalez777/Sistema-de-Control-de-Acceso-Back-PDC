package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.model.Concesionario;
import com.parquecafe.accesoapi.repository.ConcesionarioRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/concesionarios")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://front-sistema-de-control-de-acceso.vercel.app"
})
public class ConcesionarioController {

    private final ConcesionarioRepository repository;

    public ConcesionarioController(ConcesionarioRepository repository) {
        this.repository = repository;
    }

    // Lectura: la comparten Concesionario (su propia gestión) y Portería
    // (dropdown de "historial de visitas"). Admin siempre tiene acceso.
    @PreAuthorize("hasAnyRole('ADMIN','PORTERIA','CONCESIONARIO')")
    @GetMapping
    public List<Concesionario> listar() {
        return repository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN','PORTERIA','CONCESIONARIO')")
    @GetMapping("/{id}")
    public ResponseEntity<Concesionario> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear: se mantiene igual, no se bloqueó.
    @PreAuthorize("hasAnyRole('ADMIN','CONCESIONARIO')")
    @PostMapping
    public Concesionario crear(@Valid @RequestBody Concesionario concesionario) {
        return repository.save(concesionario);
    }

    // Editar: ahora EXCLUSIVO de Admin.
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Concesionario> actualizar(@PathVariable Long id, @Valid @RequestBody Concesionario datos) {
        return repository.findById(id)
                .map(existente -> {
                    existente.setNombre(datos.getNombre());
                    existente.setNit(datos.getNit());
                    existente.setActivo(datos.isActivo());
                    return ResponseEntity.ok(repository.save(existente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar: EXCLUSIVO de Admin. Si el concesionario ya tiene empleados
    // asociados, la base de datos rechaza el borrado -- en vez de dejar
    // pasar ese error crudo de SQL, se traduce a un mensaje claro que
    // sugiere desactivar en su lugar (ya existe el campo "activo" para eso).
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
                    "No se puede eliminar: este concesionario ya tiene empleados asociados. " +
                            "Márcalo como Inactivo en su lugar.");
        }
        return ResponseEntity.noContent().build();
    }
}