package com.proyecto.version1.Features.RegistrosAsistencias.task;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.Empresa;
import com.proyecto.version1.Features.Empresas.repository.EmpresaRepository;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorario;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorarioRepository;
import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import com.proyecto.version1.Features.Vacaciones.SolicitudesVacacione;
import com.proyecto.version1.Features.Vacaciones.repository.SolicitudesVacacionesRepository;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GeneracionInasistenciasTask {

    private static final Logger log = LoggerFactory.getLogger(GeneracionInasistenciasTask.class);

    private final EmpresaRepository empresaRepository;
    private final EmpleadosRepository empleadosRepository;
    private final ReglaHorarioRepository reglaHorarioRepository;
    private final RegistroAsistenciaRepository registroAsistenciaRepository;
    private final SolicitudesVacacionesRepository solicitudesVacacionesRepository;

    @Scheduled(cron = "0 */10 * * * *") // Cada 10 minutos
    @Transactional
    public void procesarInasistenciasAutomaticas() {
        log.info("Iniciando tarea programada de generación de inasistencias...");
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        // 1. Omitir si es fin de semana
        DayOfWeek diaSemana = hoy.getDayOfWeek();
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            log.info("Hoy es fin de semana ({}), omitiendo procesamiento de inasistencias.", diaSemana);
            return;
        }

        // Obtener todas las empresas registradas
        List<Empresa> empresas = empresaRepository.findAll();

        for (Empresa empresa : empresas) {
            UUID empresaId = empresa.getId();
            log.info("Procesando inasistencias para la empresa: {}", empresa.getNombre());

            // Establecer el contexto del tenant temporalmente
            TenantContext.setCurrentTenant(empresaId);
            try {
                // Obtener regla de horario para esta empresa
                List<ReglaHorario> reglas = reglaHorarioRepository.findByEmpresaId(empresaId);
                if (reglas.isEmpty()) {
                    log.warn("La empresa {} no tiene reglas de horario configuradas. Omitiendo.", empresa.getNombre());
                    continue;
                }
                ReglaHorario regla = reglas.get(0);

                // Calcular el límite de tiempo para marcar asistencia (entrada + tiempo_limite_falta_minutos)
                LocalTime limiteFalta = regla.getHoraEntradaOficial().plusMinutes(regla.getTiempoLimiteFaltaMinutos());

                // Solo procedemos si la hora actual ya superó el límite de falta
                if (ahora.isBefore(limiteFalta)) {
                    log.info("La hora actual ({}) es anterior al límite de falta ({}) para la empresa {}. Omitiendo.", 
                            ahora, limiteFalta, empresa.getNombre());
                    continue;
                }

                // Obtener todos los empleados activos de la empresa
                List<Empleado> empleados = empleadosRepository.findByEmpresaIdAndActivoTrue(empresaId);

                // Cargar todas las vacaciones aprobadas que cubren el día de hoy para esta empresa
                List<SolicitudesVacacione> vacacionesHoy = solicitudesVacacionesRepository
                        .findByEmpleado_EmpresaIdAndEstadoSolicitudAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                                empresaId, "APROBADO", hoy, hoy);

                for (Empleado empleado : empleados) {
                    // Verificar si ya tiene marcación para hoy
                    Optional<RegistroAsistencia> registro = registroAsistenciaRepository
                            .findByEmpresa_IdAndEmpleado_IdAndFecha(empresaId, empleado.getId(), hoy);

                    if (registro.isPresent()) {
                        // Ya tiene una marcación registrada, no se hace nada
                        continue;
                    }

                    // Verificar si el empleado está de vacaciones hoy
                    boolean enVacaciones = vacacionesHoy.stream()
                            .anyMatch(v -> v.getEmpleado().getId().equals(empleado.getId()));

                    if (enVacaciones) {
                        log.info("El empleado {} está de vacaciones hoy, omitiendo falta.", empleado.getNombreCompleto());
                        continue;
                    }

                    // Generar falta automática (FALTA_INJUSTIFICADA)
                    OffsetDateTime instanteEntrada = OffsetDateTime.of(hoy, regla.getHoraEntradaOficial(), ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now()));
                    OffsetDateTime instanteSalida = OffsetDateTime.of(hoy, regla.getHoraEntradaOficial(), ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())); // Cerramos con 0 horas

                    RegistroAsistencia falta = RegistroAsistencia.builder()
                            .empresa(empresa)
                            .empleado(empleado)
                            .fecha(hoy)
                            .horaEntrada(instanteEntrada)
                            .horaSalida(instanteSalida)
                            .modalidadAplicada(empleado.getModalidadPerfil() != null ? empleado.getModalidadPerfil().toUpperCase() : "PRESENCIAL")
                            .estadoEntrada("FALTA_INJUSTIFICADA")
                            .tipoRegistro("ENTRADA")
                            .instanteServidorUltimaMarcacion(OffsetDateTime.now())
                            .esFacialVerificado(false)
                            .esMockLocation(false)
                            .build();

                    registroAsistenciaRepository.save(falta);
                    log.info("Falta injustificada automática registrada para el empleado: {}", empleado.getNombreCompleto());
                }
            } catch (Exception e) {
                log.error("Error procesando inasistencias para la empresa: " + empresa.getNombre(), e);
            } finally {
                TenantContext.clear();
            }
        }
    }
}
