package com.proyecto.version1.Features.Empresas.service;

import com.proyecto.version1.Features.Empresas.dto.*;
import com.proyecto.version1.Features.Empresas.dto.DashboardMetricsResponse;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import com.proyecto.version1.Features.Empresas.EstadoLicencia;

import java.util.List;
import java.util.UUID;

public interface SuperAdminEmpresaService {
    ResponseEntity<ApiResponse<EmpresaResponse>> crearEmpresa(EmpresaCreateRequest request);
    ResponseEntity<ApiResponse<EmpresaResponse>> actualizarEmpresa(UUID id, EmpresaUpdateRequest request);
    ResponseEntity<ApiResponse<Void>> eliminarEmpresa(UUID id);
    ResponseEntity<ApiResponse<EmpresaResponse>> obtenerPorId(UUID id);
    ResponseEntity<ApiResponse<java.util.List<EmpresaResponse>>> listarEmpresas();

    /**
     * Lista empresas con filtros dinámicos y paginación.
     */
    ResponseEntity<ApiResponse<PageResponse<EmpresaResponse>>> listarEmpresasConFiltros(
            String nombre,
            String nitRut,
            String rubro,
            String estadoLicencia,
            Pageable pageable
    );
    ResponseEntity<ApiResponse<EmpresaResponse>> actualizarEstadoLicencia(UUID id, EstadoLicencia nuevoEstado);
    ResponseEntity<ApiResponse<DashboardMetricsResponse>> obtenerMetricasGlobales();
}

