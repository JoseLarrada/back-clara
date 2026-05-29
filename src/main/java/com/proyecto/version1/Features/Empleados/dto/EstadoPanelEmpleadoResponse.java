package com.proyecto.version1.Features.Empleados.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record EstadoPanelEmpleadoResponse(
        UUID empleadoId,
        String empleadoNombre,
        String modalidadPerfil,
        String modalidadAplicableHoy,
        boolean requiereQr,
        boolean requiereGps,
        boolean requiereCamara,
        boolean botonRemotoHabilitado,
        String estadoLaboral,
        boolean puedeRegistrarEntrada,
        boolean puedeRegistrarSalida,
        boolean puedeRegistrarAlmuerzo,
        LocalDate fechaServidor,
        LocalTime horaServidor,
        long timestampServidor,
        long cronometroJornadaSegundos,
        long cronometroAlmuerzoSegundos,
        long cronometroTrabajoNetoSegundos,
        OffsetDateTimeResumen registroHoy,
        List<String> alertas
) {
    public record OffsetDateTimeResumen(
            UUID registroId,
            LocalDate fecha,
            java.time.OffsetDateTime horaEntrada,
            java.time.OffsetDateTime horaAlmuerzoInicio,
            java.time.OffsetDateTime horaAlmuerzoFin,
            java.time.OffsetDateTime horaSalida,
            String estadoEntrada,
            String modalidadAplicada,
            String tipoRegistro,
            java.time.OffsetDateTime instanteServidorUltimaMarcacion
    ) {}
}

