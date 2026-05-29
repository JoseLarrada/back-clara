package com.proyecto.version1.Features.Reportes_Prenomina;

import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReporteConsolidadoResponse;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReporteGeneracionRequest;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReportePrenominaFilterRequest;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReportesPrenominaMensualResponse;
import com.proyecto.version1.Features.Reportes_Prenomina.service.ReportesPrenominaMensualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reportes-prenomina")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Pre-Nómina", description = "Generación, consulta y exportación consolidada de reportes de pre-nómina")
public class AdminReportesPrenominaMensualController {

    private final ReportesPrenominaMensualService reportesPrenominaMensualService;

    @PostMapping("/generar")
    @Operation(summary = "Generar reportes de pre-nómina", description = "Calcula y persiste la pre-nómina consolidada por empleado y período")
    public ResponseEntity generar(@Valid @RequestBody ReporteGeneracionRequest request) {
        List<ReportesPrenominaMensualResponse> generated = reportesPrenominaMensualService.generar(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/reportes-prenomina"))
                .body(generated);
    }

    @GetMapping
    @Operation(summary = "Listar reportes de pre-nómina", description = "Consulta reportes persistidos en un rango de fechas")
    public ResponseEntity listar(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @RequestParam(required = false) UUID empleadoId,
            @RequestParam(required = false) String estadoReporte,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "anioPeriodo,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = (sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        ReportePrenominaFilterRequest filter = new ReportePrenominaFilterRequest(fechaInicio, fechaFin, empleadoId, estadoReporte);
        return ResponseEntity.ok(reportesPrenominaMensualService.listar(filter, pageable));
    }

    @GetMapping("/consolidado")
    @Operation(summary = "Obtener consolidado de pre-nómina", description = "Devuelve el consolidado calculado en memoria para el rango consultado")
    public ResponseEntity consolidado(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @RequestParam(required = false) UUID empleadoId
    ) {
        return ResponseEntity.ok(reportesPrenominaMensualService.consolidar(new ReporteGeneracionRequest(fechaInicio, fechaFin, empleadoId)));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar pre-nómina a CSV", description = "Descarga el consolidado de pre-nómina en formato CSV")
    public ResponseEntity exportarCsv(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @RequestParam(required = false) UUID empleadoId
    ) {
        byte[] csv = reportesPrenominaMensualService.exportarCsv(new ReporteGeneracionRequest(fechaInicio, fechaFin, empleadoId));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pre-nomina.csv")
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Exportar pre-nómina a Excel", description = "Descarga el consolidado de pre-nómina en formato XLSX")
    public ResponseEntity exportarExcel(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @RequestParam(required = false) UUID empleadoId
    ) {
        byte[] excel = reportesPrenominaMensualService.exportarExcel(new ReporteGeneracionRequest(fechaInicio, fechaFin, empleadoId));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pre-nomina.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/export/pdf")
    @Operation(summary = "Exportar pre-nómina a PDF", description = "Descarga el consolidado de pre-nómina en formato PDF")
    public ResponseEntity exportarPdf(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @RequestParam(required = false) UUID empleadoId
    ) {
        byte[] pdf = reportesPrenominaMensualService.exportarPdf(new ReporteGeneracionRequest(fechaInicio, fechaFin, empleadoId));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pre-nomina.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reporte de pre-nómina por ID", description = "Consulta un reporte persistido específico")
    public ResponseEntity obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(reportesPrenominaMensualService.obtener(id));
    }
}


