package com.proyecto.version1.RecargosConfiguracion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "configuracion_recargos_empresa")
public class ConfiguracionRecargosEmpresa {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ColumnDefault("1.25")
    @Column(name = "factor_hora_extra_diurna", nullable = false, precision = 4, scale = 2)
    private BigDecimal factorHoraExtraDiurna;

    @ColumnDefault("1.75")
    @Column(name = "factor_hora_extra_nocturna", nullable = false, precision = 4, scale = 2)
    private BigDecimal factorHoraExtraNocturna;

    @ColumnDefault("2.00")
    @Column(name = "factor_hora_dominical_festiva", nullable = false, precision = 4, scale = 2)
    private BigDecimal factorHoraDominicalFestiva;

    @ColumnDefault("0.00")
    @Column(name = "multa_retardo_por_minuto", nullable = false, precision = 10, scale = 2)
    private BigDecimal multaRetardoPorMinuto;

}