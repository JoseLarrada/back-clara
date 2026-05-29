package com.proyecto.version1.Features.Calendario.service;

import com.proyecto.version1.Features.Calendario.dto.CalendarioHibridoLoteRequest;
import com.proyecto.version1.Features.Calendario.dto.CalendarioHibridoRequest;
import com.proyecto.version1.Features.Calendario.dto.CalendarioHibridoResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CalendarioHibridoService {
    CalendarioHibridoResponse crearCalendario(CalendarioHibridoRequest request);

    CalendarioHibridoResponse actualizarCalendario(UUID id, CalendarioHibridoRequest request);

    CalendarioHibridoResponse obtenerCalendario(UUID id);

    List<CalendarioHibridoResponse> listarCalendarios(UUID empleadoId, LocalDate desde, LocalDate hasta);

    List<CalendarioHibridoResponse> upsertCalendarioLote(CalendarioHibridoLoteRequest request);

    void eliminarCalendario(UUID id);
}

