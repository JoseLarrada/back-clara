package com.proyecto.version1.Features.Reportes_Prenomina;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empresas.Empresa;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "reportes_prenomina_mensual")
public class ReportesPrenominaMensual {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @Column(name = "mes_periodo", nullable = false)
    private Integer mesPeriodo;

    @Column(name = "anio_periodo", nullable = false)
    private Integer anioPeriodo;

    @ColumnDefault("0")
    @Column(name = "dias_trabajados_efectivos", nullable = false)
    private Integer diasTrabajadosEfectivos;

    @ColumnDefault("0")
    @Column(name = "dias_falta_injustificada", nullable = false)
    private Integer diasFaltaInjustificada;

    @ColumnDefault("0.00")
    @Column(name = "horas_extras_diurnas_totales", nullable = false, precision = 6, scale = 2)
    private BigDecimal horasExtrasDiurnasTotales;

    @ColumnDefault("0.00")
    @Column(name = "horas_extras_nocturnas_totales", nullable = false, precision = 6, scale = 2)
    private BigDecimal horasExtrasNocturnasTotales;

    @Column(name = "monto_salario_base_proporcional", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoSalarioBaseProporcional;

    @ColumnDefault("0.00")
    @Column(name = "monto_ganancia_extras", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoGananciaExtras;

    @ColumnDefault("0.00")
    @Column(name = "monto_deducciones_faltas", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoDeduccionesFaltas;

    @Column(name = "monto_neto_pagar", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoNetoPagar;

    @ColumnDefault("'BORRADOR'")
    @Column(name = "estado_reporte", nullable = false, length = 20)
    private String estadoReporte;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @CreationTimestamp
    @Column(name = "generado_el")
    private OffsetDateTime generadoEl;

}