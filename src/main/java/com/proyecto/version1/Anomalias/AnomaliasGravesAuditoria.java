package com.proyecto.version1.Anomalias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

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
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tipo_anomalia", nullable = false, length = 50)
    private String tipoAnomalia;

    @Column(name = "detalles_tecnicos", nullable = false, length = Integer.MAX_VALUE)
    private String detallesTecnicos;

    @ColumnDefault("false")
    @Column(name = "notificado_via_sns", nullable = false)
    private Boolean notificadoViaSns = false;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;

}