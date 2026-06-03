package com.proyecto.version1.Features.Geolocalizacion;

import com.proyecto.version1.Features.Empleados.Empleado;
import jakarta.persistence.*;
import lombok.*;

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
@Table(name = "ultima_ubicacion")
public class UltimaUbicacion {

    @Id
    @Column(name = "empleado_id", nullable = false)
    private UUID empleadoId;

    @TenantId
    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
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

    @Column(name = "estado_conexion", nullable = false, length = 20)
    @Builder.Default
    private String estadoConexion = "ACTIVO";

    @Column(name = "ultima_actualizacion", nullable = false)
    @Builder.Default
    private OffsetDateTime ultimaActualizacion = OffsetDateTime.now();
}

