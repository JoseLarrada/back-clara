package com.proyecto.version1.Features.Auditoria;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "logs_auditoria_sistema")
public class LogsAuditoriaSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @TenantId
    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "rol_usuario", nullable = false, length = 30)
    private String rolUsuario;

    @Column(name = "accion", nullable = false, length = 100)
    private String accion;

    @Column(name = "tabla_afectada", nullable = false, length = 50)
    private String tablaAfectada;

    @Column(name = "registro_id")
    private UUID registroId;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "valor_anterior", columnDefinition = "jsonb")
    private String valorAnterior;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "valor_nuevo", columnDefinition = "jsonb")
    private String valorNuevo;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @CreationTimestamp
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;
}
