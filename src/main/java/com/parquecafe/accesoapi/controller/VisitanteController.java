package com.parquecafe.accesoapi.controller;

import com.parquecafe.accesoapi.model.Visitante;
import com.parquecafe.accesoapi.repository.VisitanteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitantes")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://front-sistema-de-control-de-acceso.vercel.app"
})
public class VisitanteController {

    private final VisitanteRepository repository;

    public VisitanteController(VisitanteRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Visitante> listar() {
        return repository.findAll();
    }
}