package com.proyecto.version1.Empleados;

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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "empleados")
public class Empleado {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nombre_completo", nullable = false, length = 250)
    private String nombreCompleto;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ColumnDefault("'EMPLEADO'")
    @Column(name = "rol", nullable = false, length = 30)
    private String rol;

    @Column(name = "modalidad_perfil", nullable = false, length = 20)
    private String modalidadPerfil;

    @Column(name = "foto_patron_url", length = 500)
    private String fotoPatronUrl;

    @ColumnDefault("15")
    @Column(name = "saldo_vacaciones", nullable = false)
    private Integer saldoVacaciones;

    @ColumnDefault("true")
    @Column(name = "activo", nullable = false)
    private Boolean activo = false;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;

}