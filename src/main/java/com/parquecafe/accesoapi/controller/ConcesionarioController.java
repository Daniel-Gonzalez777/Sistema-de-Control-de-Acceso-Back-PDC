package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.model.Concesionario;
import com.parquecafe.accesoapi.repository.ConcesionarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concesionarios")
@CrossOrigin(origins = "http://localhost:4200")
public class ConcesionarioController {

    private final ConcesionarioRepository repository;

    public ConcesionarioController(ConcesionarioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Concesionario> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Concesionario> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Concesionario crear(@Valid @RequestBody Concesionario concesionario) {
        return repository.save(concesionario);
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
