package com.proyecto.version1.Features.Empleados;

import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionCreateRequest;
import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionResponse;
import com.proyecto.version1.Features.ContratosEmpleados.dto.ContratoResponse;
import com.proyecto.version1.Features.Empleados.dto.EstadoPanelEmpleadoResponse;
import com.proyecto.version1.Features.Empleados.dto.HistorialAsistenciaMensualResponse;
import com.proyecto.version1.Features.Empleados.dto.RegistrarAsistenciaRequest;
import com.proyecto.version1.Features.Empleados.dto.RegistroAsistenciaResponse;
import com.proyecto.version1.Features.Empleados.service.EmpleadoPanelService;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaResponse;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReportesPrenominaMensualResponse;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesEmpleadoCreateRequest;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesResponse;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesSaldoResponse;
import com.proyecto.version1.Features.Vacaciones.service.VacacionesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/empleado/panel")
@PreAuthorize("hasAnyAuthority('EMPLEADO', 'ROLE_EMPLEADO')")
@RequiredArgsConstructor
@Tag(name = "Panel del empleado", description = "Panel responsivo del empleado para dinamica de interfaz y marcacion de asistencia")
public class EmpleadoPanelController {

    private final EmpleadoPanelService empleadoPanelService;
    private final com.proyecto.version1.Features.Backups_Incidencias.service.BackupIncidenciasService backupIncidenciasService;
    private final VacacionesService vacacionesService;

    @GetMapping
    @Operation(summary = "Obtener panel del empleado", description = "Devuelve la configuracion dinamica de la interfaz y el estado laboral actual")
    public ResponseEntity<@NonNull EstadoPanelEmpleadoResponse> obtenerPanel() {
        return ResponseEntity.ok(empleadoPanelService.obtenerPanelEmpleado());
    }

    @PostMapping("/asistencia")
    @Operation(summary = "Registrar asistencia", description = "Registra entrada o salida segun la modalidad del empleado y el canal habilitado")
    public ResponseEntity<@NonNull RegistroAsistenciaResponse> registrarAsistencia(@Valid @RequestBody RegistrarAsistenciaRequest request) {
        return ResponseEntity.ok(empleadoPanelService.registrarAsistencia(request));
    }

    @GetMapping("/vacaciones/saldo")
    @Operation(summary = "Consultar saldo de vacaciones", description = "Muestra el saldo actual del empleado autenticado antes de enviar la solicitud")
    public ResponseEntity<@NonNull VacacionesSaldoResponse> consultarMiSaldoVacaciones() {
        return ResponseEntity.ok(vacacionesService.consultarMiSaldo());
    }

    @GetMapping("/geocercas")
    @Operation(summary = "Consultar mis geocercas", description = "Muestra las geocercas registradas para el empleado autenticado")
    public ResponseEntity<@NonNull List<GeocercaRemotaResponse>> consultarMisGeocercas() {
        return ResponseEntity.ok(empleadoPanelService.consultarMisGeocercas());
    }

    @GetMapping("/contrato")
    @Operation(summary = "Consultar mi contrato", description = "Muestra el contrato actual del empleado autenticado, o el más reciente si no tiene uno activo")
    public ResponseEntity<ContratoResponse> consultarMiContrato() {
        return ResponseEntity.ok(empleadoPanelService.consultarMiContrato());
    }

    @GetMapping("/reportes-prenomina")
    @Operation(summary = "Consultar mis reportes de pre-nómina", description = "Lista los reportes de pre-nómina del empleado autenticado dentro de un rango de fechas")
    public ResponseEntity<PageResponse<ReportesPrenominaMensualResponse>> consultarMisReportesPrenomina(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "anioPeriodo,desc") String sort
    ) {
        return ResponseEntity.ok(empleadoPanelService.consultarMisReportesPrenomina(fechaInicio, fechaFin, page, size, sort));
    }

    @PostMapping("/vacaciones")
    @Operation(summary = "Solicitar vacaciones", description = "Permite al empleado enviar su solicitud de vacaciones con fecha de inicio y fin")
    public ResponseEntity<@NonNull VacacionesResponse> solicitarVacaciones(
            @Valid @RequestBody VacacionesEmpleadoCreateRequest request
    ) {
        return ResponseEntity.ok(vacacionesService.crearMiSolicitud(request));
    }

    @PostMapping("/justificaciones")
    @Operation(summary = "Solicitar justificación de incidencia", description = "Permite al empleado enviar una justificación adjuntando comprobante para retardo o falta")
    public ResponseEntity<@NonNull JustificacionResponse> solicitarJustificacion(
            @Valid @RequestBody JustificacionCreateRequest request
    ) {
        return ResponseEntity.ok(backupIncidenciasService.crearJustificacionEmpleado(request));
    }

    @GetMapping("/historial-mensual")
    @Operation(summary = "Consultar historial mensual de asistencia", description = "Muestra calendario mensual con asistencias, retardos, faltas y horas acumuladas")
    public ResponseEntity<@NonNull HistorialAsistenciaMensualResponse> historialMensual(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes
    ) {
        LocalDate now = LocalDate.now();
        int anioConsulta = anio != null ? anio : now.getYear();
        int mesConsulta = mes != null ? mes : now.getMonthValue();
        return ResponseEntity.ok(empleadoPanelService.obtenerHistorialMensual(anioConsulta, mesConsulta));
    }

    @GetMapping("/ticker")
    @Operation(
            summary = "Ticker de estado laboral (SSE)",
            description = "Emite de forma periódica el estado laboral y cronómetro del empleado para actualizar la UI en tiempo real"
    )
    @SuppressWarnings("resource")
    public SseEmitter ticker(
            @RequestParam(defaultValue = "1000") long intervaloMs,
            @RequestParam(defaultValue = "300") long duracionSegundos
    ) {
        long intervaloNormalizado = Math.max(500, intervaloMs);
        long duracionNormalizada = Math.max(30, duracionSegundos);
        long timeout = (duracionNormalizada * 1000L) + 5000L;

        SseEmitter emitter = new SseEmitter(timeout);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            try {
                EstadoPanelEmpleadoResponse estado = empleadoPanelService.obtenerPanelEmpleado();
                emitter.send(SseEmitter.event()
                        .name("estado_panel")
                        .data(estado));
            } catch (IOException | IllegalStateException ex) {
                emitter.completeWithError(ex);
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, intervaloNormalizado, TimeUnit.MILLISECONDS);
        scheduler.schedule(emitter::complete, duracionNormalizada, TimeUnit.SECONDS);

        emitter.onCompletion(scheduler::shutdown);
        emitter.onTimeout(() -> {
            scheduler.shutdown();
            emitter.complete();
        });
        emitter.onError(ex -> scheduler.shutdown());

        return emitter;
    }

}
