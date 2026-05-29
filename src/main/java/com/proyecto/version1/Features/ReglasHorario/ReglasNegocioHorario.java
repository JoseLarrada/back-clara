package com.proyecto.version1.Features.ReglasHorario;

import com.proyecto.version1.Features.Empresas.Empresa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "reglas_negocio_horarios")
public class ReglasNegocioHorario {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    @Column(name = "hora_entrada_oficial", nullable = false)
    private LocalTime horaEntradaOficial;

    @Column(name = "hora_salida_oficial", nullable = false)
    private LocalTime horaSalidaOficial;

    @ColumnDefault("10")
    @Column(name = "minutos_tolerancia_retardo", nullable = false)
    private Integer minutosToleranciaRetardo;

    @ColumnDefault("120")
    @Column(name = "tiempo_limite_falta_minutos", nullable = false)
    private Integer tiempoLimiteFaltaMinutos;

}