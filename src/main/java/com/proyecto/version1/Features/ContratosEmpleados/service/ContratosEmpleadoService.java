package com.proyecto.version1.Features.ContratosEmpleados.service;

import com.proyecto.version1.Features.ContratosEmpleados.dto.*;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ContratosEmpleadoService {
    ContratoResponse crearContrato(ContratoCreateRequest request);
    ContratoResponse actualizarContrato(UUID id, ContratoUpdateRequest request);
    ContratoResponse obtenerContrato(UUID id);
    List<ContratoResponse> listarPorEmpleado(UUID empleadoId);
    PageResponse<ContratoResponse> listarContratos(Pageable pageable);
    ContratoResponse cerrarContrato(UUID id, ContratoCloseRequest request);
    ContratoResponse extenderContrato(UUID id, ContratoExtendRequest request);
    void eliminarContrato(UUID id);
}
