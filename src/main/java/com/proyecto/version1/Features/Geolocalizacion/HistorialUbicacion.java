package com.proyecto.version1.Features.Geolocalizacion;

import com.proyecto.version1.Features.Empleados.Empleado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "historial_ubicaciones")
public class HistorialUbicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @TenantId
    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "latitud", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "precision_gps", precision = 10, scale = 2)
    private BigDecimal precisionGps;

    @Column(name = "velocidad", precision = 10, scale = 2)
    private BigDecimal velocidad;

    @Column(name = "direccion", precision = 10, scale = 2)
    private BigDecimal direccion;

    @Column(name = "registrado_en", nullable = false)
    private OffsetDateTime registradoEn;

    @CreationTimestamp
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;
}
