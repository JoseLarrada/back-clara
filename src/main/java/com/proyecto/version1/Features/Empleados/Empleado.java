package com.proyecto.version1.Features.Empleados;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "empleados")
@EntityListeners(AuditingEntityListener.class)
public class Empleado implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    private UUID empresaId;

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
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}