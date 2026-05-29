package com.proyecto.version1.Features.Empresas;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "empresas")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "nit_rut", nullable = false, length = 20)
    private String nitRut;

    @Column(name = "rubro", nullable = false, length = 50)
    private String rubro;

    @ColumnDefault("50")
    @Column(name = "limite_empleados", nullable = false)
    private Integer limiteEmpleados;

    @ColumnDefault("'ACTIVO'")
    @Column(name = "estado_licencia", nullable = false, length = 20)
    private String estadoLicencia;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

}