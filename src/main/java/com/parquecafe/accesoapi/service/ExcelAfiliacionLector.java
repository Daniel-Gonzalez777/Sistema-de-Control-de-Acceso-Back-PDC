package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.dto.FilaExcelAfiliacion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lee la hoja "Plantilla" del Excel de afiliaciones y la convierte en una
 * lista de FilaExcelAfiliacion. No guarda nada en la base de datos —
 * eso lo hace CargaPlantillaService — esta clase SOLO sabe leer el archivo.
 *
 * Columnas esperadas (en este orden, fila 1 = encabezado):
 * cedula | nombre | cargo | area | concesionario_nit | afiliado_salud | eps |
 * fecha_afiliacion_salud | afiliado_pension | afp | fecha_afiliacion_pension |
 * afiliado_arl | arl | fecha_afiliacion_arl
 */
@Component
public class ExcelAfiliacionLector {

    private static final int COL_CEDULA = 0;
    private static final int COL_NOMBRE = 1;
    private static final int COL_CARGO = 2;
    private static final int COL_AREA = 3;
    private static final int COL_NIT = 4;
    private static final int COL_AFILIADO_SALUD = 5;
    private static final int COL_EPS = 6;
    private static final int COL_FECHA_SALUD = 7;
    private static final int COL_AFILIADO_PENSION = 8;
    private static final int COL_AFP = 9;
    private static final int COL_FECHA_PENSION = 10;
    private static final int COL_AFILIADO_ARL = 11;
    private static final int COL_ARL = 12;
    private static final int COL_FECHA_ARL = 13;

    public List<FilaExcelAfiliacion> leer(InputStream inputStream) throws IOException {
        List<FilaExcelAfiliacion> filas = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {

            // Busca la hoja llamada "Plantilla" (así se llama en el archivo real);
            // si no la encuentra, usa la última hoja del archivo como respaldo.
            Sheet hoja = workbook.getSheet("Plantilla");
            if (hoja == null) {
                hoja = workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
            }

            for (Row fila : hoja) {
                if (fila.getRowNum() == 0) {
                    continue; // fila 0 = encabezado, se salta
                }
                if (esFilaVacia(fila)) {
                    continue; // filas en blanco al final del archivo se ignoran
                }

                FilaExcelAfiliacion datos = new FilaExcelAfiliacion();
                datos.setNumeroFila(fila.getRowNum() + 1); // +1 porque Excel se ve 1-based

                datos.setCedula(leerTexto(fila.getCell(COL_CEDULA)));
                datos.setNombre(leerTexto(fila.getCell(COL_NOMBRE)));
                datos.setCargo(leerTexto(fila.getCell(COL_CARGO)));
                datos.setArea(leerTexto(fila.getCell(COL_AREA)));
                datos.setConcesionarioNit(leerTexto(fila.getCell(COL_NIT)));

                datos.setAfiliadoSalud(esSi(fila.getCell(COL_AFILIADO_SALUD)));
                datos.setEps(leerTexto(fila.getCell(COL_EPS)));
                datos.setFechaAfiliacionSalud(leerFecha(fila.getCell(COL_FECHA_SALUD)));

                datos.setAfiliadoPension(esSi(fila.getCell(COL_AFILIADO_PENSION)));
                datos.setAfp(leerTexto(fila.getCell(COL_AFP)));
                datos.setFechaAfiliacionPension(leerFecha(fila.getCell(COL_FECHA_PENSION)));

                datos.setAfiliadoArl(esSi(fila.getCell(COL_AFILIADO_ARL)));
                datos.setArl(leerTexto(fila.getCell(COL_ARL)));
                datos.setFechaAfiliacionArl(leerFecha(fila.getCell(COL_FECHA_ARL)));

                filas.add(datos);
            }
        }

        return filas;
    }

    // --- helpers de lectura de celdas ---
    // Excel puede guardar el mismo dato como texto, número o fecha nativa,
    // dependiendo de cómo lo haya escrito la persona. Estos métodos lo
    // normalizan todo a String/boolean/LocalDate sin importar el formato original.
    // Se usa DataFormatter (en vez de mutar celda.setCellType) porque es la
    // forma recomendada por POI y evita el clásico bug de que una cédula
    // numérica termine leyéndose como "1002345678.0".
    private final DataFormatter dataFormatter = new DataFormatter(Locale.forLanguageTag("es-CO"));

    private String leerTexto(Cell celda) {
        if (celda == null) return null;
        String valor = dataFormatter.formatCellValue(celda);
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private boolean esSi(Cell celda) {
        String valor = leerTexto(celda);
        return valor != null && valor.equalsIgnoreCase("SI");
    }

    private LocalDate leerFecha(Cell celda) {
        if (celda == null) return null;

        if (celda.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(celda)) {
            return celda.getLocalDateTimeCellValue().toLocalDate();
        }

        String texto = leerTexto(celda);
        if (texto == null) return null;

        try {
            return LocalDate.parse(texto); // espera formato yyyy-MM-dd
        } catch (Exception e) {
            return null; // fecha mal escrita -> se deja null, el service decide si eso es un error
        }
    }

    private boolean esFilaVacia(Row fila) {
        Cell primeraCelda = fila.getCell(COL_CEDULA);
        return primeraCelda == null || leerTexto(primeraCelda) == null;
    }
}
