package com.proyecto.version1.Features.Vacaciones;

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
@Table(name = "movimientos_vacaciones")
public class MovimientoVacaciones {
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

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    @Column(name = "cantidad_dias", nullable = false)
    private Integer cantidadDias;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "solicitud_id")
    private SolicitudesVacacione solicitud;

    @Column(name = "motivo_ajuste", length = Integer.MAX_VALUE)
    private String motivoAjuste;

    @Column(name = "creado_por")
    private UUID creadoPor;

    @CreationTimestamp
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;
}
