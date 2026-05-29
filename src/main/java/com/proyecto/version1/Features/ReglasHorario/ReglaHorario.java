package com.proyecto.version1.Features.ReglasHorario;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "reglas_negocio_horarios")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReglaHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    @Column(name = "hora_entrada_oficial", nullable = false)
    private LocalTime horaEntradaOficial;

    @Column(name = "hora_salida_oficial", nullable = false)
    private LocalTime horaSalidaOficial;

    @Column(name = "minutos_tolerancia_retardo", nullable = false)
    private Integer minutosToleranciaRetardo;

    @Column(name = "tiempo_limite_falta_minutos", nullable = false)
    private Integer tiempoLimiteFaltaMinutos;
}

