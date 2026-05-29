package com.proyecto.version1.Features.ReglasHorario.service;

import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioCreateRequest;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioResponse;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioUpdateRequest;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ReglaHorarioService {

    ReglaHorarioResponse crear(ReglaHorarioCreateRequest request);

    List<ReglaHorarioResponse> listarPorEmpresa(UUID empresaId);

    PageResponse<ReglaHorarioResponse> listarPorEmpresaConPaginacion(UUID empresaId, Pageable pageable);

    ReglaHorarioResponse obtener(UUID reglaHorarioId);

    ReglaHorarioResponse actualizar(UUID reglaHorarioId, ReglaHorarioUpdateRequest request);

    void eliminar(UUID reglaHorarioId);

    ReglaHorarioResponse obtenerPorEmpresa(UUID empresaId);
}

