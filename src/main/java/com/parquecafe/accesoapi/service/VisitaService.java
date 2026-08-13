package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.dto.RegistroVisitaRequest;
import com.parquecafe.accesoapi.model.Empleado;
import com.parquecafe.accesoapi.model.RegistroVisita;
import com.parquecafe.accesoapi.model.Visitante;
import com.parquecafe.accesoapi.repository.EmpleadoRepository;
import com.parquecafe.accesoapi.repository.RegistroVisitaRepository;
import com.parquecafe.accesoapi.repository.VisitanteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VisitaService {

    private final VisitanteRepository visitanteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RegistroVisitaRepository registroVisitaRepository;

    public VisitaService(VisitanteRepository visitanteRepository,
                          EmpleadoRepository empleadoRepository,
                          RegistroVisitaRepository registroVisitaRepository) {
        this.visitanteRepository = visitanteRepository;
        this.empleadoRepository = empleadoRepository;
        this.registroVisitaRepository = registroVisitaRepository;
    }

    // CU-04: registrar ingreso de visitante
    public RegistroVisita registrarIngreso(RegistroVisitaRequest request) {
        Empleado empleado = empleadoRepository.findById(request.getEmpleadoVisitadoId())
                .orElseThrow(() -> new EntityNotFoundException("Empleado a visitar no encontrado"));

        Visitante visitante = new Visitante();
        visitante.setNombre(request.getNombreVisitante());
        visitante.setDocumento(request.getDocumentoVisitante());
        visitante = visitanteRepository.save(visitante);

        RegistroVisita registro = new RegistroVisita();
        registro.setVisitante(visitante);
        registro.setEmpleadoVisitado(empleado);
        registro.setArea(empleado.getArea());
        registro.setMotivo(request.getMotivo());
        registro.setIngresaVehiculo(request.isIngresaVehiculo());
        registro.setPlacaVehiculo(request.getPlacaVehiculo());
        registro.setTipoVehiculo(request.getTipoVehiculo());
        registro.setZonaParqueo(request.getZonaParqueo());

        return registroVisitaRepository.save(registro);
    }

    // CU-05: registrar salida de visitante
    public RegistroVisita registrarSalida(Long registroVisitaId) {
        RegistroVisita registro = registroVisitaRepository.findById(registroVisitaId)
                .orElseThrow(() -> new EntityNotFoundException("Registro de visita no encontrado"));
        registro.setFechaHoraSalida(LocalDateTime.now());
        return registroVisitaRepository.save(registro);
    }
}
