package com.proyecto.version1.Features.Empleados.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RegistroAsistenciaResponse(
        UUID id,
        UUID empleadoId,
        String empleadoNombre,
        LocalDate fecha,
        OffsetDateTime horaEntrada,
        OffsetDateTime horaAlmuerzoInicio,
        OffsetDateTime horaAlmuerzoFin,
        OffsetDateTime horaSalida,
        String modalidadAplicada,
        String estadoEntrada,
        String tipoRegistroPersistido,
        OffsetDateTime instanteServidorUltimaMarcacion,
        Boolean esFacialVerificado,
        BigDecimal precisionGpsAccuracy,
        String tokenQrUtilizado,
        TipoMarcacionAsistencia tipoMarcacionRegistrada,
        OrigenMarcacionAsistencia origenMarcacion,
        String mensaje
) {}

