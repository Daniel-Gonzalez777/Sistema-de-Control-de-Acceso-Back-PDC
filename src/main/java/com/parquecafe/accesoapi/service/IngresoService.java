package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.dto.CalendarioMensualDTO;
import com.parquecafe.accesoapi.dto.DiaCalendarioDTO;
import com.parquecafe.accesoapi.dto.MovimientoCalendarioDTO;
import com.parquecafe.accesoapi.dto.ResultadoValidacionDTO;
import com.parquecafe.accesoapi.model.*;
import com.parquecafe.accesoapi.repository.AfiliacionRepository;
import com.parquecafe.accesoapi.repository.ConcesionarioRepository;
import com.parquecafe.accesoapi.repository.EmpleadoRepository;
import com.parquecafe.accesoapi.repository.RegistroIngresoEmpleadoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class IngresoService {

    private final EmpleadoRepository empleadoRepository;
    private final AfiliacionRepository afiliacionRepository;
    private final RegistroIngresoEmpleadoRepository registroRepository;
    private final ConcesionarioRepository concesionarioRepository;

    public IngresoService(EmpleadoRepository empleadoRepository,
                          AfiliacionRepository afiliacionRepository,
                          RegistroIngresoEmpleadoRepository registroRepository,
                          ConcesionarioRepository concesionarioRepository) {
        this.empleadoRepository = empleadoRepository;
        this.afiliacionRepository = afiliacionRepository;
        this.registroRepository = registroRepository;
        this.concesionarioRepository = concesionarioRepository;
    }

    /**
     * Punto único de validación. Recibe el número de cédula (hoy digitado,
     * mañana leído por un escáner).
     *
     * No se registra salida: cada empleado puede registrar UNA sola entrada
     * autorizada por día. Si ya la registró hoy y vuelve a escanear su
     * cédula, el sistema le indica que ya ingresó (sin volver a autorizar
     * ni a crear una nueva entrada). Al cambiar el día, puede volver a
     * entrar con normalidad -- esto ocurre solo porque la validación
     * compara contra la fecha de HOY, no porque algo "lo saque" a
     * medianoche.
     */
    public ResultadoValidacionDTO validarIngreso(String cedula) {

        Optional<Empleado> empleadoOpt = empleadoRepository.findByCedula(cedula);

        if (empleadoOpt.isEmpty()) {
            guardarRegistro(cedula, null, ResultadoIngreso.NO_AUTORIZADO, TipoMovimiento.ENTRADA,
                    "Persona no registrada en el sistema");
            return new ResultadoValidacionDTO(false, null, null,
                    "Persona no registrada en el sistema", TipoMovimiento.ENTRADA);
        }

        Empleado empleado = empleadoOpt.get();
        Concesionario concesionario = empleado.getConcesionario();

        // --- ¿Ya registró su ingreso HOY? No se autoriza de nuevo. ---
        if (estaActualmenteDentro(empleado)) {
            String horaTexto = registroRepository
                    .findTopByEmpleadoAndTipoMovimientoAndResultadoOrderByFechaHoraDesc(
                            empleado, TipoMovimiento.ENTRADA, ResultadoIngreso.AUTORIZADO)
                    .map(r -> r.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .orElse("");
            String motivo = "Ya registró su ingreso hoy" + (horaTexto.isEmpty() ? "" : " a las " + horaTexto);
            guardarRegistro(cedula, empleado, ResultadoIngreso.NO_AUTORIZADO, TipoMovimiento.ENTRADA, motivo);
            return new ResultadoValidacionDTO(false, empleado.getNombre(), concesionario.getNombre(), motivo, TipoMovimiento.ENTRADA);
        }

        // --- A partir de aquí, es un intento de ENTRADA: se valida todo. ---

        // RN-04: concesionario inactivo -> nunca autorizado
        if (!concesionario.isActivo()) {
            String motivo = "El concesionario " + concesionario.getNombre() + " está inactivo";
            guardarRegistro(cedula, empleado, ResultadoIngreso.NO_AUTORIZADO, TipoMovimiento.ENTRADA, motivo);
            return new ResultadoValidacionDTO(false, empleado.getNombre(), concesionario.getNombre(), motivo, TipoMovimiento.ENTRADA);
        }

        // RN-02/RN-03: se valida contra la afiliación del mes en curso.
        YearMonth ahora = YearMonth.now();
        Optional<Afiliacion> afiliacionOpt = afiliacionRepository
                .findByEmpleadoAndAnioAndMes(empleado, ahora.getYear(), ahora.getMonthValue());

        if (afiliacionOpt.isEmpty()) {
            String motivo = "El concesionario no ha cargado la plantilla del mes actual para este empleado";
            guardarRegistro(cedula, empleado, ResultadoIngreso.NO_AUTORIZADO, TipoMovimiento.ENTRADA, motivo);
            return new ResultadoValidacionDTO(false, empleado.getNombre(), concesionario.getNombre(), motivo, TipoMovimiento.ENTRADA);
        }

        Afiliacion afiliacion = afiliacionOpt.get();

        // Junta TODOS los faltantes en vez de detenerse en el primero,
        // para que el vigilante vea de una vez todo lo que hay que arreglar.
        List<String> faltantes = new ArrayList<>();
        if (!afiliacion.isAfiliadoSalud()) {
            faltantes.add("Salud (EPS)");
        }
        if (!afiliacion.isAfiliadoPension()) {
            faltantes.add("Pensión (AFP)");
        }
        if (!afiliacion.isAfiliadoARL()) {
            faltantes.add("ARL");
        }

        if (!faltantes.isEmpty()) {
            String motivo = "No se encuentra afiliación vigente a: " + String.join(", ", faltantes);
            guardarRegistro(cedula, empleado, ResultadoIngreso.NO_AUTORIZADO, TipoMovimiento.ENTRADA, motivo);
            return new ResultadoValidacionDTO(false, empleado.getNombre(), concesionario.getNombre(), motivo, TipoMovimiento.ENTRADA);
        }

        // Todo en orden -> entrada autorizada (la única del día para este empleado)
        guardarRegistro(cedula, empleado, ResultadoIngreso.AUTORIZADO, TipoMovimiento.ENTRADA, "OK");
        return new ResultadoValidacionDTO(true, empleado.getNombre(), concesionario.getNombre(), "OK", TipoMovimiento.ENTRADA);
    }

    /**
     * True si el empleado ya registró una entrada AUTORIZADA hoy. Busca
     * específicamente la última entrada AUTORIZADA (no "el último registro
     * sea cual sea"), porque los intentos repetidos de "ya ingresó" también
     * quedan guardados como NO_AUTORIZADO -- si mirara solo el último
     * registro sin filtrar, un tercer intento dejaría de detectar la
     * entrada original y volvería a autorizar por error.
     *
     * Al pasar la medianoche, esto vuelve a dar false automáticamente
     * porque compara contra la fecha de hoy, sin necesidad de ningún
     * proceso que lo reinicie.
     *
     * Público porque también lo usa VisitaService: no tiene sentido registrar
     * una visita a un empleado de concesionario que no ha ingresado hoy.
     */
    public boolean estaActualmenteDentro(Empleado empleado) {
        Optional<RegistroIngresoEmpleado> ultimaEntradaAutorizada = registroRepository
                .findTopByEmpleadoAndTipoMovimientoAndResultadoOrderByFechaHoraDesc(
                        empleado, TipoMovimiento.ENTRADA, ResultadoIngreso.AUTORIZADO);
        return ultimaEntradaAutorizada.isPresent()
                && ultimaEntradaAutorizada.get().getFechaHora().toLocalDate().equals(LocalDate.now());
    }

    // Historial completo de movimientos.
    public java.util.List<RegistroIngresoEmpleado> obtenerHistorial() {
        return registroRepository.findAllByOrderByFechaHoraDesc();
    }

    // Empleados que ya registraron su entrada autorizada HOY.
    public java.util.List<RegistroIngresoEmpleado> obtenerEmpleadosDentro() {
        java.util.List<RegistroIngresoEmpleado> historial = obtenerHistorial();
        java.util.Map<Long, RegistroIngresoEmpleado> ultimoMovimientoPorEmpleado = new java.util.LinkedHashMap<>();

        for (RegistroIngresoEmpleado registro : historial) {
            if (registro.getEmpleado() == null) continue; // cédulas no registradas no cuentan
            Long empleadoId = registro.getEmpleado().getId();
            ultimoMovimientoPorEmpleado.putIfAbsent(empleadoId, registro); // el primero que aparece es el más reciente
        }

        LocalDate hoy = LocalDate.now();
        return ultimoMovimientoPorEmpleado.values().stream()
                .filter(r -> r.getResultado() == ResultadoIngreso.AUTORIZADO
                        && r.getTipoMovimiento() == TipoMovimiento.ENTRADA
                        && r.getFechaHora().toLocalDate().equals(hoy))
                .toList();
    }

    /**
     * Calendario mensual de UN concesionario: día por día, quién entró y
     * quién salió (y a qué hora). Siempre filtra por concesionarioId, así
     * que nunca mezcla información de dos empresas distintas.
     */
    public CalendarioMensualDTO obtenerCalendarioMensual(Long concesionarioId, int anio, int mes) {
        Concesionario concesionario = concesionarioRepository.findById(concesionarioId)
                .orElseThrow(() -> new NoSuchElementException("Concesionario no encontrado: " + concesionarioId));

        YearMonth ym = YearMonth.of(anio, mes);
        LocalDateTime desde = ym.atDay(1).atStartOfDay();
        LocalDateTime hasta = ym.atEndOfMonth().atTime(23, 59, 59);

        List<RegistroIngresoEmpleado> registros = registroRepository
                .findByEmpleado_Concesionario_IdAndFechaHoraBetweenOrderByFechaHoraAsc(concesionarioId, desde, hasta);

        // Agrupa por día del mes (1..31), conservando el orden cronológico dentro de cada día.
        Map<Integer, List<MovimientoCalendarioDTO>> porDia = new TreeMap<>();
        for (RegistroIngresoEmpleado r : registros) {
            int dia = r.getFechaHora().getDayOfMonth();
            String nombre = r.getEmpleado() != null ? r.getEmpleado().getNombre() : null;

            MovimientoCalendarioDTO mov = new MovimientoCalendarioDTO(
                    nombre,
                    r.getCedulaConsultada(),
                    r.getFechaHora().toLocalTime(),
                    r.getTipoMovimiento(),
                    r.getResultado(),
                    r.getMotivo()
            );
            porDia.computeIfAbsent(dia, d -> new ArrayList<>()).add(mov);
        }

        List<DiaCalendarioDTO> dias = new ArrayList<>();
        for (Map.Entry<Integer, List<MovimientoCalendarioDTO>> entry : porDia.entrySet()) {
            dias.add(new DiaCalendarioDTO(entry.getKey(), entry.getValue()));
        }

        return new CalendarioMensualDTO(concesionarioId, concesionario.getNombre(), anio, mes, dias);
    }

    private void guardarRegistro(String cedula, Empleado empleado, ResultadoIngreso resultado,
                                 TipoMovimiento tipoMovimiento, String motivo) {
        RegistroIngresoEmpleado registro = new RegistroIngresoEmpleado();
        registro.setCedulaConsultada(cedula);
        registro.setEmpleado(empleado);
        registro.setResultado(resultado);
        registro.setTipoMovimiento(tipoMovimiento);
        registro.setMotivo(motivo);
        registroRepository.save(registro);
    }
}