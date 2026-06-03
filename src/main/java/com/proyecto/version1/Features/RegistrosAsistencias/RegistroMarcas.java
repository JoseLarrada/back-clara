package com.proyecto.version1.Features.RegistrosAsistencias;

import com.proyecto.version1.Features.Empleados.Empleado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "registro_marcas")
public class RegistroMarcas {
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

    @Column(name = "tipo_marca", nullable = false, length = 20)
    private String tipoMarca;

    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;

    @Column(name = "modalidad", nullable = false, length = 20)
    private String modalidad;

    @Column(name = "foto_captura_url", length = 500)
    private String fotoCapturaUrl;

    @Column(name = "score_facial_coincidencia", precision = 5, scale = 2)
    private BigDecimal scoreFacialCoincidencia;

    @Column(name = "latitud", precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "precision_gps_accuracy", precision = 10, scale = 2)
    private BigDecimal precisionGpsAccuracy;

    @ColumnDefault("false")
    @Column(name = "es_mock_location", nullable = false)
    @Builder.Default
    private Boolean esMockLocation = false;

    @CreationTimestamp
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;
}
