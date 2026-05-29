package com.proyecto.version1.Features.ReglasHorario.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReglaHorarioCreateRequest {

    private UUID empresaId;

    @NotBlank(message = "La descripción es requerida")
    @Size(min = 1, max = 100, message = "La descripción debe tener entre 1 y 100 caracteres")
    private String descripcion;

    @NotNull(message = "La hora de entrada oficial es requerida")
    private LocalTime horaEntradaOficial;

    @NotNull(message = "La hora de salida oficial es requerida")
    private LocalTime horaSalidaOficial;

    @NotNull(message = "Los minutos de tolerancia para retardo son requeridos")
    @Min(value = 0, message = "Los minutos de tolerancia deben ser mayor o igual a 0")
    @Max(value = 480, message = "Los minutos de tolerancia no pueden ser mayor a 480 (8 horas)")
    private Integer minutosToleranciaRetardo;

    @NotNull(message = "El tiempo límite de falta (en minutos) es requerido")
    @Min(value = 1, message = "El tiempo límite de falta debe ser mayor a 0")
    @Max(value = 1440, message = "El tiempo límite de falta no puede ser mayor a 1440 (24 horas)")
    private Integer tiempoLimiteFaltaMinutos;
}

