package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.model.Concesionario;
import com.parquecafe.accesoapi.repository.ConcesionarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    // Crear/editar/eliminar: solo Concesionario y Admin.
    @PreAuthorize("hasAnyRole('ADMIN','CONCESIONARIO')")
    @PostMapping
    public Concesionario crear(@Valid @RequestBody Concesionario concesionario) {
        return repository.save(concesionario);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CONCESIONARIO')")
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

    @PreAuthorize("hasAnyRole('ADMIN','CONCESIONARIO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}