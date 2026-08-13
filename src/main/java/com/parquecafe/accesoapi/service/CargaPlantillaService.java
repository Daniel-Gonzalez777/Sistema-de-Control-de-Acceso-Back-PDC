package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.dto.ErrorFilaDTO;
import com.parquecafe.accesoapi.dto.FilaExcelAfiliacion;
import com.parquecafe.accesoapi.dto.ResultadoCargaDTO;
import com.parquecafe.accesoapi.model.Afiliacion;
import com.parquecafe.accesoapi.model.Concesionario;
import com.parquecafe.accesoapi.model.Empleado;
import com.parquecafe.accesoapi.repository.AfiliacionRepository;
import com.parquecafe.accesoapi.repository.ConcesionarioRepository;
import com.parquecafe.accesoapi.repository.EmpleadoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Corazón de la carga masiva: recibe el Excel completo y, fila por fila,
 * decide si debe crear o actualizar el empleado, y si debe crear o
 * actualizar su afiliación del mes actual. Un error en una fila NO
 * detiene el proceso de las demás — se acumula y se reporta al final.
 */
@Service
public class CargaPlantillaService {

    private final ExcelAfiliacionLector lector;
    private final ConcesionarioRepository concesionarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AfiliacionRepository afiliacionRepository;

    public CargaPlantillaService(ExcelAfiliacionLector lector,
                                  ConcesionarioRepository concesionarioRepository,
                                  EmpleadoRepository empleadoRepository,
                                  AfiliacionRepository afiliacionRepository) {
        this.lector = lector;
        this.concesionarioRepository = concesionarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.afiliacionRepository = afiliacionRepository;
    }

    public ResultadoCargaDTO cargarPlantilla(MultipartFile archivo, Long concesionarioId) throws IOException {

        Concesionario concesionario = concesionarioRepository.findById(concesionarioId)
                .orElseThrow(() -> new EntityNotFoundException("Concesionario no encontrado (id=" + concesionarioId + ")"));

        List<FilaExcelAfiliacion> filas = lector.leer(archivo.getInputStream());

        int exitosas = 0;
        int creados = 0;
        int actualizados = 0;
        List<ErrorFilaDTO> errores = new ArrayList<>();

        YearMonth mesActual = YearMonth.now();

        for (FilaExcelAfiliacion fila : filas) {
            try {
                validarFila(fila, concesionario);

                Optional<Empleado> empleadoExistente = empleadoRepository.findByCedula(fila.getCedula());
                boolean esNuevo = empleadoExistente.isEmpty();

                Empleado empleado = empleadoExistente.orElseGet(Empleado::new);
                empleado.setCedula(fila.getCedula());
                empleado.setNombre(fila.getNombre());
                empleado.setCargo(fila.getCargo());
                empleado.setArea(fila.getArea());
                empleado.setConcesionario(concesionario);
                empleado = empleadoRepository.save(empleado);

                // Busca si ya existe afiliación de este empleado para el mes actual
                // (por ejemplo, si el concesionario sube el archivo dos veces por
                // corrección) para actualizarla en vez de duplicarla.
                Afiliacion afiliacion = afiliacionRepository
                        .findByEmpleadoAndAnioAndMes(empleado, mesActual.getYear(), mesActual.getMonthValue())
                        .orElseGet(Afiliacion::new);

                afiliacion.setEmpleado(empleado);
                afiliacion.setAnio(mesActual.getYear());
                afiliacion.setMes(mesActual.getMonthValue());

                afiliacion.setAfiliadoSalud(fila.isAfiliadoSalud());
                afiliacion.setEps(fila.getEps());
                afiliacion.setFechaAfiliacionSalud(fila.getFechaAfiliacionSalud());

                afiliacion.setAfiliadoPension(fila.isAfiliadoPension());
                afiliacion.setAfp(fila.getAfp());
                afiliacion.setFechaAfiliacionPension(fila.getFechaAfiliacionPension());

                afiliacion.setAfiliadoARL(fila.isAfiliadoArl());
                afiliacion.setArl(fila.getArl());
                afiliacion.setFechaAfiliacionARL(fila.getFechaAfiliacionArl());

                afiliacion.setFechaCarga(LocalDateTime.now());

                afiliacionRepository.save(afiliacion);

                exitosas++;
                if (esNuevo) {
                    creados++;
                } else {
                    actualizados++;
                }

            } catch (Exception e) {
                errores.add(new ErrorFilaDTO(fila.getNumeroFila(), fila.getCedula(), e.getMessage()));
            }
        }

        return new ResultadoCargaDTO(filas.size(), exitosas, errores.size(), creados, actualizados, errores);
    }

    /**
     * Validaciones antes de guardar nada. Si algo falla acá, la fila entera
     * se rechaza y se reporta como error, pero el resto del archivo sigue.
     */
    private void validarFila(FilaExcelAfiliacion fila, Concesionario concesionario) {
        if (fila.getCedula() == null || fila.getCedula().isBlank()) {
            throw new IllegalArgumentException("La cédula está vacía");
        }
        if (fila.getNombre() == null || fila.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre está vacío");
        }
        if (fila.getConcesionarioNit() == null || fila.getConcesionarioNit().isBlank()) {
            throw new IllegalArgumentException("El NIT del concesionario está vacío en esta fila");
        }
        // Cruce de seguridad: el NIT de la fila debe coincidir con el
        // concesionario que se seleccionó al subir el archivo. Esto evita que,
        // por error, se suban empleados de una empresa a nombre de otra.
        if (!fila.getConcesionarioNit().trim().equalsIgnoreCase(concesionario.getNit().trim())) {
            throw new IllegalArgumentException(
                    "El NIT de la fila (" + fila.getConcesionarioNit() + ") no coincide con el del concesionario seleccionado (" + concesionario.getNit() + ")"
            );
        }
    }
}
