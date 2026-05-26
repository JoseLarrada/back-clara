package com.proyecto.version1.Calendario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "calendario_hibrido")
public class CalendarioHibrido {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "caracter_dia", nullable = false, length = 20)
    private String caracterDia;

}