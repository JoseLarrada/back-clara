package com.proyecto.version1.Features.GeocercasRemota.service;

import com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaRequest;
import com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaResponse;

import java.util.List;
import java.util.UUID;

public interface GeocercasRemotaService {
    GeocercaRemotaResponse crearGeocerca(GeocercaRemotaRequest request);

    GeocercaRemotaResponse actualizarGeocerca(UUID id, GeocercaRemotaRequest request);

    GeocercaRemotaResponse obtenerGeocerca(UUID id);

    List<GeocercaRemotaResponse> listarGeocercas(UUID empleadoId);

    void eliminarGeocerca(UUID id);
}

