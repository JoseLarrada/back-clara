package com.proyecto.version1.RegistrosAsistencias;

import com.proyecto.version1.Empleados.Empleado;
import com.proyecto.version1.Empresas.Empresa;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "registro_asistencia")
public class RegistroAsistencia {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ColumnDefault("CURRENT_DATE")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada", nullable = false)
    private OffsetDateTime horaEntrada;

    @Column(name = "hora_salida")
    private OffsetDateTime horaSalida;

    @Column(name = "modalidad_aplicada", nullable = false, length = 20)
    private String modalidadAplicada;

    @ColumnDefault("'A_TIEMPO'")
    @Column(name = "estado_entrada", nullable = false, length = 20)
    private String estadoEntrada;

    @ColumnDefault("false")
    @Column(name = "es_facial_verificado", nullable = false)
    private Boolean esFacialVerificado = false;

    @Column(name = "precision_gps_accuracy", precision = 5, scale = 2)
    private BigDecimal precisionGpsAccuracy;

    @Column(name = "token_qr_utilizado")
    private String tokenQrUtilizado;

}