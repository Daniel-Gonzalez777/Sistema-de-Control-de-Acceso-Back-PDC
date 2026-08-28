package com.parquecafe.accesoapi.service;

import com.parquecafe.accesoapi.dto.CalendarioMensualDTO;
import com.parquecafe.accesoapi.dto.DiaCalendarioDTO;
import com.parquecafe.accesoapi.dto.MovimientoCalendarioDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Convierte el calendario mensual de un concesionario en un archivo .xlsx,
 * una fila por movimiento, ordenado día por día (RF de exportación).
 */
@Service
public class CalendarioExcelService {

    private static final String[] NOMBRES_MES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    public ByteArrayOutputStream generarExcel(CalendarioMensualDTO calendario) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            String tituloHoja = NOMBRES_MES[calendario.getMes() - 1] + " " + calendario.getAnio();
            Sheet sheet = workbook.createSheet(tituloHoja);

            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloAutorizado = crearEstiloEstado(workbook, IndexedColors.LIGHT_GREEN.getIndex());
            CellStyle estiloNoAutorizado = crearEstiloEstado(workbook, IndexedColors.ROSE.getIndex());

            int filaActual = 0;

            // Fila de título con el nombre del concesionario y el mes.
            Row filaTitulo = sheet.createRow(filaActual++);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("Calendario de accesos — " + calendario.getConcesionarioNombre()
                    + " — " + tituloHoja);
            filaActual++; // fila en blanco

            // Encabezados de columna
            Row encabezado = sheet.createRow(filaActual++);
            String[] columnas = {"Día", "Hora", "Empleado", "Cédula", "Movimiento", "Resultado", "Motivo"};
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = encabezado.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (DiaCalendarioDTO dia : calendario.getDias()) {
                for (MovimientoCalendarioDTO mov : dia.getMovimientos()) {
                    Row fila = sheet.createRow(filaActual++);

                    fila.createCell(0).setCellValue(dia.getDia());
                    fila.createCell(1).setCellValue(mov.getHora().format(formatoHora));
                    fila.createCell(2).setCellValue(mov.getNombreEmpleado() != null ? mov.getNombreEmpleado() : "(no registrado)");
                    fila.createCell(3).setCellValue(mov.getCedula());
                    fila.createCell(4).setCellValue(mov.getTipoMovimiento().name());

                    Cell celdaResultado = fila.createCell(5);
                    celdaResultado.setCellValue(mov.getResultado().name());
                    celdaResultado.setCellStyle(
                            mov.getResultado().name().equals("AUTORIZADO") ? estiloAutorizado : estiloNoAutorizado);

                    fila.createCell(6).setCellValue(mov.getMotivo() != null ? mov.getMotivo() : "");
                }
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out;
        }
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(font);
        estilo.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private CellStyle crearEstiloEstado(Workbook workbook, short colorIndex) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setFillForegroundColor(colorIndex);
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }
}
