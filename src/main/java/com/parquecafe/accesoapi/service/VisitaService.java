package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.dto.RegistroVisitaRequest;
import com.parquecafe.accesoapi.model.Empleado;
import com.parquecafe.accesoapi.model.EmpleadoDirectoParque;
import com.parquecafe.accesoapi.model.RegistroVisita;
import com.parquecafe.accesoapi.model.Visitante;
import com.parquecafe.accesoapi.repository.EmpleadoDirectoParqueRepository;
import com.parquecafe.accesoapi.repository.EmpleadoRepository;
import com.parquecafe.accesoapi.repository.RegistroVisitaRepository;
import com.parquecafe.accesoapi.repository.VisitanteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitaService {

    // Lista fija de áreas válidas para un empleado DIRECTO del parque
    // (no de un concesionario). Coincide con lo que pidió el negocio.
    public static final List<String> AREAS_EMPLEADO_DIRECTO = List.of(
            "Servicios generales", "Sistemas", "Contaduría", "Taquilla",
            "Almacenes", "Atracciones", "Mantenimiento", "Fontanería",
            "Servicio al cliente", "Recursos humanos", "Jardinería", "Alimentos"
    );

    private final VisitanteRepository visitanteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoDirectoParqueRepository empleadoDirectoRepository;
    private final RegistroVisitaRepository registroVisitaRepository;
    private final IngresoService ingresoService;

    public VisitaService(VisitanteRepository visitanteRepository,
                         EmpleadoRepository empleadoRepository,
                         EmpleadoDirectoParqueRepository empleadoDirectoRepository,
                         RegistroVisitaRepository registroVisitaRepository,
                         IngresoService ingresoService) {
        this.visitanteRepository = visitanteRepository;
        this.empleadoRepository = empleadoRepository;
        this.empleadoDirectoRepository = empleadoDirectoRepository;
        this.registroVisitaRepository = registroVisitaRepository;
        this.ingresoService = ingresoService;
    }

    // CU-04: registrar ingreso de visitante
    public RegistroVisita registrarIngreso(RegistroVisitaRequest request) {

        RegistroVisita registro = new RegistroVisita();

        // --- 1) Resolver a quién visita: empleado del sistema O empleado directo ---
        boolean tieneEmpleadoSistema = request.getEmpleadoVisitadoId() != null;
        boolean tieneEmpleadoDirecto = request.getEmpleadoDirectoCedula() != null
                && !request.getEmpleadoDirectoCedula().isBlank();

        if (tieneEmpleadoSistema && tieneEmpleadoDirecto) {
            throw new IllegalArgumentException(
                    "Debe indicar SOLO un empleado a visitar: del sistema o directo del parque, no ambos.");
        }

        if (tieneEmpleadoSistema) {
            Empleado empleado = empleadoRepository.findById(request.getEmpleadoVisitadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Empleado a visitar no encontrado"));

            // Solo aplica a empleados de concesionario: no se puede registrar una
            // visita a alguien que ni siquiera está actualmente dentro del Parque.
            if (!ingresoService.estaActualmenteDentro(empleado)) {
                throw new IllegalArgumentException(
                        "No se puede registrar la visita: " + empleado.getNombre()
                                + " no se encuentra actualmente dentro del Parque.");
            }

            registro.setEmpleadoVisitado(empleado);
            registro.setArea(empleado.getArea());

        } else if (tieneEmpleadoDirecto) {
            String area = request.getEmpleadoDirectoArea();
            if (area == null || !AREAS_EMPLEADO_DIRECTO.contains(area)) {
                throw new IllegalArgumentException(
                        "Área de empleado directo inválida. Debe ser una de: " + AREAS_EMPLEADO_DIRECTO);
            }

            // Buscar por cédula; si ya existe lo reutiliza (y actualiza nombre/área
            // por si cambiaron), si no existe lo crea.
            EmpleadoDirectoParque directo = empleadoDirectoRepository
                    .findByCedula(request.getEmpleadoDirectoCedula())
                    .orElseGet(EmpleadoDirectoParque::new);

            directo.setCedula(request.getEmpleadoDirectoCedula());
            directo.setNombre(request.getEmpleadoDirectoNombre());
            directo.setArea(area);
            directo = empleadoDirectoRepository.save(directo);

            registro.setEmpleadoDirecto(directo);
            registro.setArea(directo.getArea());

        } else {
            throw new IllegalArgumentException(
                    "Debe indicar un empleado a visitar: del sistema (empleadoVisitadoId) "
                            + "o directo del parque (cédula, nombre y área).");
        }

        // --- 2) Resolver al visitante: reutilizar si ya existe por documento ---
        Visitante visitante = visitanteRepository.findByDocumento(request.getDocumentoVisitante())
                .orElseGet(Visitante::new);
        visitante.setNombre(request.getNombreVisitante());
        visitante.setDocumento(request.getDocumentoVisitante());
        visitante = visitanteRepository.save(visitante);

        // --- 3) Completar y guardar el registro de visita ---
        registro.setVisitante(visitante);
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

    // Historial de visitas recibidas por un empleado del sistema
    public List<RegistroVisita> historialPorEmpleado(Long empleadoId) {
        return registroVisitaRepository.findByEmpleadoVisitadoIdOrderByFechaHoraEntradaDesc(empleadoId);
    }

    // Historial de visitas recibidas por un empleado directo del parque
    public List<RegistroVisita> historialPorEmpleadoDirecto(Long empleadoDirectoId) {
        return registroVisitaRepository.findByEmpleadoDirectoIdOrderByFechaHoraEntradaDesc(empleadoDirectoId);
    }

    // Historial de visitas hechas por un visitante
    public List<RegistroVisita> historialPorVisitante(Long visitanteId) {
        return registroVisitaRepository.findByVisitanteIdOrderByFechaHoraEntradaDesc(visitanteId);
    }
}