package com.proyecto.version1.Features.Backups_Incidencias;

import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "registro_asistencia_id", nullable = false)
    private RegistroAsistencia registroAsistencia;

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
    @CreationTimestamp
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;

}