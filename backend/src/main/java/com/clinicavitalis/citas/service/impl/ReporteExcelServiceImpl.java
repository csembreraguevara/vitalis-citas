package com.clinicavitalis.citas.service.impl;

import com.clinicavitalis.citas.dto.CitaDTO;
import com.clinicavitalis.citas.dto.OcupacionDTO;
import com.clinicavitalis.citas.dto.OcupacionMedicoDTO;
import com.clinicavitalis.citas.exception.ReglaNegocioException;
import com.clinicavitalis.citas.repository.ReporteRepository;
import com.clinicavitalis.citas.service.ReporteDetalleService;
import com.clinicavitalis.citas.service.ReporteExcelService;
import com.clinicavitalis.citas.service.ReporteService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Genera el reporte R01 en Excel con tres hojas: por especialidad, por médico y detalle de citas.
 * <ul>
 *   <li>Apache POI: crea el libro .xlsx, estilos y fórmulas SUM para los totales.</li>
 *   <li>Google Guava: {@link Range} y {@link Preconditions} para validar el periodo.</li>
 *   <li>Apache Commons Lang: {@link StringUtils} para limpiar los textos de las celdas.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ReporteExcelServiceImpl implements ReporteExcelService {

    private static final Logger log = LoggerFactory.getLogger(ReporteExcelServiceImpl.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<String> COLUMNAS_CONTEO = List.of(
            "Por confirmar", "Confirmadas", "Atendidas", "No asistió", "Canceladas", "Total");
    private static final List<String> COLUMNAS_DETALLE = List.of(
            "Fecha", "Hora", "Especialidad", "Médico", "Consultorio", "Paciente", "Estado", "Canal", "Motivo");

    private static final Map<String, String> ESTADOS = Map.of(
            "PENDIENTE", "Por confirmar", "CONFIRMADA", "Confirmada", "ATENDIDA", "Atendida",
            "NO_ASISTIO", "No asistió", "CANCELADA", "Cancelada");

    private final ReporteService reporteService;
    private final ReporteDetalleService reporteDetalleService;
    private final ReporteRepository reporteRepository;

    public ReporteExcelServiceImpl(ReporteService reporteService, ReporteDetalleService reporteDetalleService,
                                   ReporteRepository reporteRepository) {
        this.reporteService = reporteService;
        this.reporteDetalleService = reporteDetalleService;
        this.reporteRepository = reporteRepository;
    }

    @Override
    public byte[] ocupacion(LocalDate desde, LocalDate hasta) {
        Preconditions.checkNotNull(desde, "desde es obligatorio");
        Preconditions.checkNotNull(hasta, "hasta es obligatorio");
        // Guava Range: el periodo válido va de "desde" hasta un año después.
        Range<LocalDate> permitido = Range.closed(desde, desde.plusDays(ReporteServiceImpl.MAX_DIAS_RANGO));
        if (!permitido.contains(hasta)) {
            throw new ReglaNegocioException("El periodo debe empezar antes de terminar y no pasar de un año.");
        }

        List<OcupacionDTO> especialidades = reporteService.ocupacion(desde, hasta);
        List<OcupacionMedicoDTO> medicos = reporteDetalleService.ocupacionPorMedico(desde, hasta);
        List<CitaDTO> citas = reporteRepository.citasDelPeriodo(desde, hasta).stream().map(CitaDTO::de).toList();
        String periodo = "Del " + desde.format(FECHA) + " al " + hasta.format(FECHA);

        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            hojaEspecialidades(libro, estilos, periodo, especialidades);
            hojaMedicos(libro, estilos, periodo, medicos);
            hojaDetalle(libro, estilos, periodo, citas);
            libro.write(salida);
            log.info("Reporte Excel de ocupación generado ({}): {} especialidades, {} citas",
                    periodo, especialidades.size(), citas.size());
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel", ex);
        }
    }

    private void hojaEspecialidades(Workbook libro, Estilos estilos, String periodo, List<OcupacionDTO> filas) {
        Sheet hoja = libro.createSheet("Por especialidad");
        int fila = encabezado(hoja, estilos, "Ocupación de citas por especialidad", periodo,
                "Especialidad", COLUMNAS_CONTEO);
        int primeraFila = fila;
        for (OcupacionDTO dato : filas) {
            Row row = hoja.createRow(fila++);
            texto(row, 0, dato.especialidad(), estilos.celda);
            conteos(row, 1, estilos, dato.pendientes(), dato.confirmadas(), dato.atendidas(),
                    dato.noAsistio(), dato.canceladas(), dato.total());
        }
        filaTotales(hoja, estilos, fila, primeraFila, COLUMNAS_CONTEO.size());
        ajustarColumnas(hoja, COLUMNAS_CONTEO.size() + 1);
    }

    private void hojaMedicos(Workbook libro, Estilos estilos, String periodo, List<OcupacionMedicoDTO> filas) {
        Sheet hoja = libro.createSheet("Por médico");
        List<String> columnas = new ArrayList<>(List.of("Especialidad"));
        columnas.addAll(COLUMNAS_CONTEO);
        int fila = encabezado(hoja, estilos, "Ocupación de citas por médico", periodo, "Médico", columnas);
        int primeraFila = fila;
        for (OcupacionMedicoDTO dato : filas) {
            Row row = hoja.createRow(fila++);
            texto(row, 0, dato.medico(), estilos.celda);
            texto(row, 1, dato.especialidad(), estilos.celda);
            conteos(row, 2, estilos, dato.pendientes(), dato.confirmadas(), dato.atendidas(),
                    dato.noAsistio(), dato.canceladas(), dato.total());
        }
        filaTotales(hoja, estilos, fila, primeraFila, columnas.size());
        ajustarColumnas(hoja, columnas.size() + 1);
    }

    private void hojaDetalle(Workbook libro, Estilos estilos, String periodo, List<CitaDTO> citas) {
        Sheet hoja = libro.createSheet("Detalle de citas");
        int fila = encabezado(hoja, estilos, "Detalle de citas", periodo,
                COLUMNAS_DETALLE.get(0), COLUMNAS_DETALLE.subList(1, COLUMNAS_DETALLE.size()));
        for (CitaDTO cita : citas) {
            Row row = hoja.createRow(fila++);
            texto(row, 0, cita.fecha().format(FECHA), estilos.celda);
            texto(row, 1, cita.hora().toString().substring(0, 5), estilos.celda);
            texto(row, 2, cita.especialidad(), estilos.celda);
            texto(row, 3, cita.medico(), estilos.celda);
            texto(row, 4, cita.consultorio(), estilos.celda);
            texto(row, 5, cita.paciente(), estilos.celda);
            texto(row, 6, ESTADOS.getOrDefault(cita.estado(), cita.estado()), estilos.celda);
            texto(row, 7, "PORTAL".equals(cita.canal()) ? "Portal web" : "Recepción", estilos.celda);
            texto(row, 8, StringUtils.abbreviate(StringUtils.defaultIfBlank(cita.motivo(), "—"), 120),
                    estilos.celda);
        }
        hoja.createFreezePane(0, 4);
        ajustarColumnas(hoja, COLUMNAS_DETALLE.size());
    }

    /** Título, periodo y fila de encabezados. Devuelve la primera fila libre para datos. */
    private int encabezado(Sheet hoja, Estilos estilos, String titulo, String periodo,
                           String primeraColumna, List<String> columnas) {
        texto(hoja.createRow(0), 0, "Clínica Vitalis · " + titulo, estilos.titulo);
        texto(hoja.createRow(1), 0, periodo, estilos.subtitulo);
        Row cabecera = hoja.createRow(3);
        texto(cabecera, 0, primeraColumna, estilos.cabecera);
        for (int i = 0; i < columnas.size(); i++) {
            texto(cabecera, i + 1, columnas.get(i), estilos.cabecera);
        }
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, Math.max(3, columnas.size())));
        return 4;
    }

    private void conteos(Row row, int columnaInicial, Estilos estilos, long... valores) {
        for (int i = 0; i < valores.length; i++) {
            Cell celda = row.createCell(columnaInicial + i);
            celda.setCellValue(valores[i]);
            celda.setCellStyle(estilos.numero);
        }
    }

    /** Fila de totales con fórmulas SUM, para que Excel recalcule si se edita una cifra. */
    private void filaTotales(Sheet hoja, Estilos estilos, int fila, int primeraFila, int columnasConteo) {
        if (fila == primeraFila) {
            texto(hoja.createRow(fila), 0, "No hay citas registradas en ese periodo.", estilos.subtitulo);
            return;
        }
        Row total = hoja.createRow(fila);
        texto(total, 0, "Total", estilos.total);
        int ultimaColumna = hoja.getRow(3).getLastCellNum() - 1;
        for (int col = ultimaColumna - COLUMNAS_CONTEO.size() + 1; col <= ultimaColumna; col++) {
            String letra = CellReference.convertNumToColString(col);
            Cell celda = total.createCell(col);
            celda.setCellFormula("SUM(" + letra + (primeraFila + 1) + ":" + letra + fila + ")");
            celda.setCellStyle(estilos.totalNumero);
        }
        for (int col = 1; col < ultimaColumna - COLUMNAS_CONTEO.size() + 1; col++) {
            total.createCell(col).setCellStyle(estilos.total);
        }
    }

    private void texto(Row row, int columna, String valor, CellStyle estilo) {
        Cell celda = row.createCell(columna);
        celda.setCellValue(StringUtils.defaultString(valor));
        celda.setCellStyle(estilo);
    }

    private void ajustarColumnas(Sheet hoja, int cantidad) {
        for (int col = 0; col < cantidad; col++) {
            hoja.autoSizeColumn(col);
            hoja.setColumnWidth(col, Math.min(hoja.getColumnWidth(col) + 512, 60 * 256));
        }
    }

    /** Estilos reutilizables del libro (POI recomienda crearlos una sola vez). */
    private static final class Estilos {
        final CellStyle titulo;
        final CellStyle subtitulo;
        final CellStyle cabecera;
        final CellStyle celda;
        final CellStyle numero;
        final CellStyle total;
        final CellStyle totalNumero;

        Estilos(Workbook libro) {
            Font negrita = libro.createFont();
            negrita.setBold(true);
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            Font blanca = libro.createFont();
            blanca.setBold(true);
            blanca.setColor(IndexedColors.WHITE.getIndex());

            titulo = libro.createCellStyle();
            titulo.setFont(fuenteTitulo);
            subtitulo = libro.createCellStyle();

            cabecera = conBorde(libro.createCellStyle());
            cabecera.setFont(blanca);
            cabecera.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            celda = conBorde(libro.createCellStyle());
            numero = conBorde(libro.createCellStyle());

            total = conBorde(libro.createCellStyle());
            total.setFont(negrita);
            total.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            total.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalNumero = libro.createCellStyle();
            totalNumero.cloneStyleFrom(total);
        }

        private static CellStyle conBorde(CellStyle estilo) {
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
            return estilo;
        }
    }
}
