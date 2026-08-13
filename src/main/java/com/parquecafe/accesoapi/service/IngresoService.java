package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.dto.ResultadoValidacionDTO;
import com.parquecafe.accesoapi.model.*;
import com.parquecafe.accesoapi.repository.AfiliacionRepository;
import com.parquecafe.accesoapi.repository.EmpleadoRepository;
import com.parquecafe.accesoapi.repository.RegistroIngresoEmpleadoRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Optional;

@Service
public class IngresoService {

    private final EmpleadoRepository empleadoRepository;
    private final AfiliacionRepository afiliacionRepository;
    private final RegistroIngresoEmpleadoRepository registroRepository;

    public IngresoService(EmpleadoRepository empleadoRepository,
                           AfiliacionRepository afiliacionRepository,
                           RegistroIngresoEmpleadoRepository registroRepository) {
        this.empleadoRepository = empleadoRepository;
        this.afiliacionRepository = afiliacionRepository;
        this.registroRepository = registroRepository;
    }

    /**
     * Punto único de validación. Recibe el número de cédula (hoy digitado,
     * mañana leído por un escáner) y decide dos cosas:
     *
     *   1) Si la persona NO está actualmente dentro del Parque -> es un
     *      intento de ENTRADA, se aplican las reglas de negocio de siempre
     *      (RN-01 a RN-04: concesionario activo + Salud + Pensión + ARL).
     *
     *   2) Si la persona YA está dentro (su último movimiento autorizado
     *      fue una ENTRADA) -> este escaneo se toma como su SALIDA,
     *      sin volver a validar afiliación (para salir no hace falta
     *      estar afiliado, solo para entrar). Esto es justamente lo que
     *      impide que alguien "vuelva a entrar" sin haber salido antes:
     *      el segundo escaneo consecutivo no es una nueva entrada, es
     *      la salida de la primera.
     *
     * Todo movimiento, autorizado o no, queda guardado (RN-05) y forma
     * el historial de entradas/salidas.
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

        // --- ¿La persona ya está adentro? Si el último movimiento suyo fue
        //     una ENTRADA autorizada, este escaneo es su SALIDA. ---
        if (estaActualmenteDentro(empleado)) {
            guardarRegistro(cedula, empleado, ResultadoIngreso.AUTORIZADO, TipoMovimiento.SALIDA,
                    "Salida registrada");
            return new ResultadoValidacionDTO(true, empleado.getNombre(), concesionario.getNombre(),
                    "Salida registrada", TipoMovimiento.SALIDA);
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

        if (!afiliacion.isAfiliadoSalud()) {
            String motivo = "No se encuentra afiliación vigente a Salud (EPS)";
            guardarRegistro(cedula, empleado, ResultadoIngreso.NO_AUTORIZADO, TipoMovimiento.ENTRADA, motivo);
            return new ResultadoValidacionDTO(false, empleado.getNombre(), concesionario.getNombre(), motivo, TipoMovimiento.ENTRADA);
        }

        if (!afiliacion.isAfiliadoPension()) {
            String motivo = "No se encuentra afiliación vigente a Pensión (AFP)";
            guardarRegistro(cedula, empleado, ResultadoIngreso.NO_AUTORIZADO, TipoMovimiento.ENTRADA, motivo);
            return new ResultadoValidacionDTO(false, empleado.getNombre(), concesionario.getNombre(), motivo, TipoMovimiento.ENTRADA);
        }

        if (!afiliacion.isAfiliadoARL()) {
            String motivo = "No se encuentra afiliación vigente a ARL";
            guardarRegistro(cedula, empleado, ResultadoIngreso.NO_AUTORIZADO, TipoMovimiento.ENTRADA, motivo);
            return new ResultadoValidacionDTO(false, empleado.getNombre(), concesionario.getNombre(), motivo, TipoMovimiento.ENTRADA);
        }

        // Todo en orden -> entrada autorizada
        guardarRegistro(cedula, empleado, ResultadoIngreso.AUTORIZADO, TipoMovimiento.ENTRADA, "OK");
        return new ResultadoValidacionDTO(true, empleado.getNombre(), concesionario.getNombre(), "OK", TipoMovimiento.ENTRADA);
    }

    /**
     * True si el último movimiento AUTORIZADO de este empleado fue una ENTRADA
     * (es decir, todavía no ha registrado la salida correspondiente).
     */
    private boolean estaActualmenteDentro(Empleado empleado) {
        Optional<RegistroIngresoEmpleado> ultimo = registroRepository.findTopByEmpleadoOrderByFechaHoraDesc(empleado);
        return ultimo.isPresent()
                && ultimo.get().getResultado() == ResultadoIngreso.AUTORIZADO
                && ultimo.get().getTipoMovimiento() == TipoMovimiento.ENTRADA;
    }

    // Historial completo de movimientos (entradas y salidas, autorizados o no).
    public java.util.List<RegistroIngresoEmpleado> obtenerHistorial() {
        return registroRepository.findAllByOrderByFechaHoraDesc();
    }

    // Empleados que en este momento están dentro del Parque (entraron y no han
    // registrado salida). Recorre el historial (ya viene del más reciente al
    // más antiguo) y se queda con el primer movimiento visto de cada empleado.
    public java.util.List<RegistroIngresoEmpleado> obtenerEmpleadosDentro() {
        java.util.List<RegistroIngresoEmpleado> historial = obtenerHistorial();
        java.util.Map<Long, RegistroIngresoEmpleado> ultimoMovimientoPorEmpleado = new java.util.LinkedHashMap<>();

        for (RegistroIngresoEmpleado registro : historial) {
            if (registro.getEmpleado() == null) continue; // cédulas no registradas no cuentan
            Long empleadoId = registro.getEmpleado().getId();
            ultimoMovimientoPorEmpleado.putIfAbsent(empleadoId, registro); // el primero que aparece es el más reciente
        }

        return ultimoMovimientoPorEmpleado.values().stream()
                .filter(r -> r.getResultado() == ResultadoIngreso.AUTORIZADO && r.getTipoMovimiento() == TipoMovimiento.ENTRADA)
                .toList();
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
