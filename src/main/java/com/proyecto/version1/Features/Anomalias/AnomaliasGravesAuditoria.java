package com.proyecto.version1.Features.Anomalias;

import com.proyecto.version1.Features.Empleados.Empleado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import org.hibernate.annotations.TenantId;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "anomalias_graves_auditoria")
public class AnomaliasGravesAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @TenantId
    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "tipo_anomalia", nullable = false, length = 50)
    private String tipoAnomalia;

    @Column(name = "detalles_tecnicos", nullable = false, length = Integer.MAX_VALUE)
    private String detallesTecnicos;

    @ColumnDefault("false")
    @Column(name = "notificado_via_sns", nullable = false)
    @Builder.Default
    private Boolean notificadoViaSns = false;

    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(name = "comentario", length = Integer.MAX_VALUE)
    private String comentario;

    @CreationTimestamp
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;
}