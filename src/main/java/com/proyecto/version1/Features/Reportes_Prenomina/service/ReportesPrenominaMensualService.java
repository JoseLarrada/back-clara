package com.proyecto.version1.Features.Reportes_Prenomina.service;

import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReporteConsolidadoResponse;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReporteGeneracionRequest;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReportePrenominaFilterRequest;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReportesPrenominaMensualResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ReportesPrenominaMensualService {

    List<ReportesPrenominaMensualResponse> generar(ReporteGeneracionRequest request);

    PageResponse<ReportesPrenominaMensualResponse> listar(ReportePrenominaFilterRequest filter, Pageable pageable);

    ReportesPrenominaMensualResponse obtener(UUID id);

    List<ReporteConsolidadoResponse> consolidar(ReporteGeneracionRequest request);

    byte[] exportarCsv(ReporteGeneracionRequest request);

    byte[] exportarExcel(ReporteGeneracionRequest request);

    byte[] exportarPdf(ReporteGeneracionRequest request);
}

