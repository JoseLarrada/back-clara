package com.proyecto.version1.ContratosEmpleados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "contratos_empleados")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratosEmpleado {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "salario_base_mensual", nullable = false, precision = 12, scale = 2)
    private BigDecimal salarioBaseMensual;

    @ColumnDefault("'COP'")
    @Column(name = "tipo_moneda", nullable = false, length = 3)
    private String tipoMoneda;

    @ColumnDefault("'TERMINO_INDEFINIDO'")
    @Column(name = "tipo_contrato", nullable = false, length = 50)
    private String tipoContrato;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_retiro")
    private LocalDate fechaRetiro;

    @ColumnDefault("true")
    @Column(name = "activo", nullable = false)
    private Boolean activo = false;

}