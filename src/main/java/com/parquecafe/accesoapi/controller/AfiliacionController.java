package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.dto.ResultadoCargaDTO;
import com.parquecafe.accesoapi.model.Afiliacion;
import com.parquecafe.accesoapi.repository.AfiliacionRepository;
import com.parquecafe.accesoapi.service.CargaPlantillaService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/afiliaciones")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://front-sistema-de-control-de-acceso-7gthyuj7w.vercel.app"
})
public class AfiliacionController {

    private final AfiliacionRepository repository;
    private final CargaPlantillaService cargaPlantillaService;

    public AfiliacionController(AfiliacionRepository repository, CargaPlantillaService cargaPlantillaService) {
        this.repository = repository;
        this.cargaPlantillaService = cargaPlantillaService;
    }

    // Endpoint clave: recibe el Excel completo de un concesionario y
    // crea/actualiza en bloque sus empleados y afiliaciones del mes actual.
    // Se manda como multipart/form-data (archivo adjunto), no como JSON.
    @PostMapping(value = "/cargar-plantilla", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoCargaDTO cargarPlantilla(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("concesionarioId") Long concesionarioId) throws IOException {

        return cargaPlantillaService.cargarPlantilla(archivo, concesionarioId);
    }

    @GetMapping
    public List<Afiliacion> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Afiliacion> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Afiliacion crear(@Valid @RequestBody Afiliacion afiliacion) {
        return repository.save(afiliacion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Afiliacion> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Afiliacion afiliacion) {

        return repository.findById(id)
                .map(actual -> {
                    afiliacion.setId(id);
                    return ResponseEntity.ok(repository.save(afiliacion));
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