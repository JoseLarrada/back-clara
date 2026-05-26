package com.proyecto.version1.Backups_Incidencias;

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
@Table(name = "backup_incidencias_justificaciones")
public class BackupIncidenciasJustificacione {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "motivo_empleado", nullable = false, length = Integer.MAX_VALUE)
    private String motivoEmpleado;

    @Column(name = "url_comprobante_s3", nullable = false, length = 500)
    private String urlComprobanteS3;

    @ColumnDefault("'PENDIENTE'")
    @Column(name = "estado_solicitud", nullable = false, length = 20)
    private String estadoSolicitud;

    @Column(name = "comentarios_administrador", length = Integer.MAX_VALUE)
    private String comentariosAdministrador;

    @Column(name = "procesado_en")
    private OffsetDateTime procesadoEn;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;

}