package com.proyecto.version1.Features.RecargosConfiguracion.service;

import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosCreateRequest;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosResponse;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosUpdateRequest;

import java.util.UUID;

public interface ConfiguracionRecargosService {

    ConfiguracionRecargosResponse crear(ConfiguracionRecargosCreateRequest request);

    ConfiguracionRecargosResponse obtener(UUID id);

    ConfiguracionRecargosResponse actualizar(UUID id, ConfiguracionRecargosUpdateRequest request);

    ConfiguracionRecargosResponse obtenerPorEmpresa(UUID empresaId);

    void eliminar(UUID id);
}

