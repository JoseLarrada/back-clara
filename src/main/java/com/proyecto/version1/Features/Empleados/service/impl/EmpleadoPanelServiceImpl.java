package com.proyecto.version1.Features.Empleados.service.impl;

import com.proyecto.version1.Features.Calendario.repository.CalendarioHibridoRepository;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.dto.EstadoPanelEmpleadoResponse;
import com.proyecto.version1.Features.Empleados.dto.HistorialAsistenciaMensualResponse;
import com.proyecto.version1.Features.Empleados.dto.OrigenMarcacionAsistencia;
import com.proyecto.version1.Features.Empleados.dto.RegistrarAsistenciaRequest;
import com.proyecto.version1.Features.Empleados.dto.RegistroAsistenciaResponse;
import com.proyecto.version1.Features.Empleados.dto.TipoMarcacionAsistencia;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empleados.service.EmpleadoPanelService;
import com.proyecto.version1.Features.Empresas.Empresa;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.Features.Empresas.repository.EmpresaRepository;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorario;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorarioRepository;
import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import com.proyecto.version1.security.TenantContext;
import com.proyecto.version1.Features.Anomalias.service.AlertaService;
import com.proyecto.version1.Features.GeocercasRemota.repository.GeocercasRemotaRepository;
import com.proyecto.version1.Features.Geolocalizacion.GeoUtils;
import com.proyecto.version1.Features.RegistrosAsistencias.service.QrValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpleadoPanelServiceImpl implements EmpleadoPanelService {

    private static final LocalTime HORA_ALMUERZO_INICIO = LocalTime.of(12, 0);
    private static final LocalTime HORA_ALMUERZO_FIN = LocalTime.of(14, 0);

    private final EmpleadosRepository empleadosRepository;
    private final EmpresaRepository empresaRepository;
    private final RegistroAsistenciaRepository registroAsistenciaRepository;
    private final ReglaHorarioRepository reglaHorarioRepository;
    private final CalendarioHibridoRepository calendarioHibridoRepository;
    private final AlertaService alertaService;
    private final QrValidationService qrValidationService;
    private final GeocercasRemotaRepository geocercasRemotaRepository;

    @Override
    @Transactional(readOnly = true)
    public EstadoPanelEmpleadoResponse obtenerPanelEmpleado() {
        UUID tenantId = requireTenant();
        Empleado empleado = requireCurrentEmployee(tenantId);
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        String modalidadAplicableHoy = modalidadAplicableHoy(empleado, hoy);
        Optional<RegistroAsistencia> registroHoy = registroHoy(tenantId, empleado.getId(), hoy);
        String estadoLaboral = calcularEstadoLaboral(ahora, registroHoy.orElse(null));
        long timestampServidor = System.currentTimeMillis();
        long cronometroJornadaSegundos = calcularCronometroJornadaSegundos(registroHoy.orElse(null));
        long cronometroAlmuerzoSegundos = calcularCronometroAlmuerzoSegundos(registroHoy.orElse(null));
        long cronometroTrabajoNetoSegundos = Math.max(cronometroJornadaSegundos - cronometroAlmuerzoSegundos, 0);

        boolean requiereQr = "PRESENCIAL".equalsIgnoreCase(modalidadAplicableHoy);
        boolean requiereGps = "REMOTO".equalsIgnoreCase(modalidadAplicableHoy);
        boolean requiereCamara = "REMOTO".equalsIgnoreCase(modalidadAplicableHoy);
        boolean botonRemotoHabilitado = "REMOTO".equalsIgnoreCase(modalidadAplicableHoy);

        List<String> alertas = new ArrayList<>();
        if ("HIBRIDO".equalsIgnoreCase(empleado.getModalidadPerfil()) && calendarioHibridoRepository.findByEmpleado_IdAndFecha(empleado.getId(), hoy).isEmpty()) {
            alertas.add("Empleado hibrido sin calendario asignado para hoy; se asumio presencial.");
        }
        if (requiereQr) {
            alertas.add("Validacion QR habilitada para marcacion presencial.");
        }
        if (requiereGps || requiereCamara) {
            alertas.add("Validacion GPS/Camara habilitada para marcacion remota.");
        }
        if (registroHoy.isEmpty()) {
            alertas.add("Aun no existe registro de asistencia para hoy.");
        } else if (registroHoy.get().getHoraSalida() == null) {
            alertas.add("La jornada de hoy sigue abierta.");
        }

        return new EstadoPanelEmpleadoResponse(
                empleado.getId(),
                empleado.getNombreCompleto(),
                empleado.getModalidadPerfil(),
                modalidadAplicableHoy,
                requiereQr,
                requiereGps,
                requiereCamara,
                botonRemotoHabilitado,
                estadoLaboral,
                registroHoy.isEmpty(),
                registroHoy.map(registro -> registro.getHoraSalida() == null).orElse(false),
                registroHoy.map(registro -> registro.getHoraEntrada() != null && registro.getHoraSalida() == null).orElse(false),
                hoy,
                ahora,
                timestampServidor,
                cronometroJornadaSegundos,
                cronometroAlmuerzoSegundos,
                cronometroTrabajoNetoSegundos,
                registroHoy.map(this::toRegistroHoyResumen).orElse(null),
                alertas
        );
    }

    @Override
    public RegistroAsistenciaResponse registrarAsistencia(RegistrarAsistenciaRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = requireCurrentEmployee(tenantId);
        Empresa empresa = requireEmpresa(tenantId);
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();
        String modalidadAplicableHoy = modalidadAplicableHoy(empleado, hoy);
        ReglaHorario regla = obtenerRegla(tenantId);
        Optional<RegistroAsistencia> registroHoy = registroHoy(tenantId, empleado.getId(), hoy);

        validarMarcacionSeguridad(request, empleado, tenantId, modalidadAplicableHoy);

        if (request.tipoMarcacion() == TipoMarcacionAsistencia.ENTRADA) {
            if (registroHoy.isPresent()) {
                throw new BadRequestException("Ya existe un registro de entrada para hoy.");
            }

            RegistroAsistencia nuevo = RegistroAsistencia.builder()
                    .empresa(empresa)
                    .empleado(empleado)
                    .fecha(hoy)
                    .horaEntrada(OffsetDateTime.of(ahora, ZoneId.systemDefault().getRules().getOffset(ahora)))
                    .horaSalida(null)
                    .horaAlmuerzoInicio(null)
                    .horaAlmuerzoFin(null)
                    .modalidadAplicada(modalidadAplicableHoy)
                    .estadoEntrada(calcularEstadoEntrada(ahora.toLocalTime(), regla))
                    .tipoRegistro("ENTRADA")
                    .instanteServidorUltimaMarcacion(OffsetDateTime.of(ahora, ZoneId.systemDefault().getRules().getOffset(ahora)))
                    .esFacialVerificado(Boolean.TRUE.equals(request.esFacialVerificado()))
                    .precisionGpsAccuracy(request.precisionGpsAccuracy())
                    .tokenQrUtilizado(tokenQrAUsar(request.origenMarcacion(), request.tokenQr()))
                    .latitud(request.latitud())
                    .longitud(request.longitud())
                    .esMockLocation(Boolean.TRUE.equals(request.esMockLocation()))
                    .fotoCapturaUrl(request.fotoCapturaUrl())
                    .scoreFacialCoincidencia(request.scoreFacialCoincidencia() != null ? BigDecimal.valueOf(request.scoreFacialCoincidencia()) : null)
                    .build();

            RegistroAsistencia saved = registroAsistenciaRepository.save(nuevo);
            return new RegistroAsistenciaResponse(
                    saved.getId(),
                    empleado.getId(),
                    empleado.getNombreCompleto(),
                    saved.getFecha(),
                    saved.getHoraEntrada(),
                    saved.getHoraAlmuerzoInicio(),
                    saved.getHoraAlmuerzoFin(),
                    saved.getHoraSalida(),
                    saved.getModalidadAplicada(),
                    saved.getEstadoEntrada(),
                    saved.getTipoRegistro(),
                    saved.getInstanteServidorUltimaMarcacion(),
                    saved.getEsFacialVerificado(),
                    saved.getPrecisionGpsAccuracy(),
                    saved.getTokenQrUtilizado(),
                    saved.getLatitud(),
                    saved.getLongitud(),
                    saved.getEsMockLocation(),
                    saved.getFotoCapturaUrl(),
                    saved.getScoreFacialCoincidencia() != null ? saved.getScoreFacialCoincidencia().doubleValue() : null,
                    request.tipoMarcacion(),
                    request.origenMarcacion(),
                    "Entrada registrada correctamente"
            );
        }

        if (request.tipoMarcacion() == TipoMarcacionAsistencia.ALMUERZO) {
            RegistroAsistencia existente = registroHoy
                    .orElseThrow(() -> new BadRequestException("No existe una entrada de hoy para registrar almuerzo."));

            if (existente.getHoraSalida() != null) {
                throw new BadRequestException("La jornada ya finalizo, no se puede registrar almuerzo.");
            }

            OffsetDateTime momentoServidor = OffsetDateTime.of(ahora, ZoneId.systemDefault().getRules().getOffset(ahora));
            String mensaje;
            if (existente.getHoraAlmuerzoInicio() == null) {
                existente.setHoraAlmuerzoInicio(momentoServidor);
                existente.setTipoRegistro("ALMUERZO");
                existente.setInstanteServidorUltimaMarcacion(momentoServidor);
                mensaje = "Inicio de almuerzo registrado correctamente";
            } else if (existente.getHoraAlmuerzoFin() == null) {
                if (momentoServidor.isBefore(existente.getHoraAlmuerzoInicio())) {
                    throw new BadRequestException("La hora de fin de almuerzo no puede ser anterior al inicio.");
                }
                existente.setHoraAlmuerzoFin(momentoServidor);
                existente.setTipoRegistro("ALMUERZO");
                existente.setInstanteServidorUltimaMarcacion(momentoServidor);
                mensaje = "Fin de almuerzo registrado correctamente";
            } else {
                throw new BadRequestException("El almuerzo de hoy ya fue registrado completamente.");
            }

            if (request.precisionGpsAccuracy() != null) {
                existente.setPrecisionGpsAccuracy(request.precisionGpsAccuracy());
            }
            if (request.tokenQr() != null && !request.tokenQr().isBlank()) {
                existente.setTokenQrUtilizado(request.tokenQr());
            }
            if (request.latitud() != null) existente.setLatitud(request.latitud());
            if (request.longitud() != null) existente.setLongitud(request.longitud());
            if (request.esMockLocation() != null) existente.setEsMockLocation(request.esMockLocation());
            if (request.fotoCapturaUrl() != null) existente.setFotoCapturaUrl(request.fotoCapturaUrl());
            if (request.scoreFacialCoincidencia() != null) {
                existente.setScoreFacialCoincidencia(BigDecimal.valueOf(request.scoreFacialCoincidencia()));
            }

            RegistroAsistencia saved = registroAsistenciaRepository.save(existente);
            return new RegistroAsistenciaResponse(
                    saved.getId(),
                    empleado.getId(),
                    empleado.getNombreCompleto(),
                    saved.getFecha(),
                    saved.getHoraEntrada(),
                    saved.getHoraAlmuerzoInicio(),
                    saved.getHoraAlmuerzoFin(),
                    saved.getHoraSalida(),
                    saved.getModalidadAplicada(),
                    saved.getEstadoEntrada(),
                    saved.getTipoRegistro(),
                    saved.getInstanteServidorUltimaMarcacion(),
                    saved.getEsFacialVerificado(),
                    saved.getPrecisionGpsAccuracy(),
                    saved.getTokenQrUtilizado(),
                    saved.getLatitud(),
                    saved.getLongitud(),
                    saved.getEsMockLocation(),
                    saved.getFotoCapturaUrl(),
                    saved.getScoreFacialCoincidencia() != null ? saved.getScoreFacialCoincidencia().doubleValue() : null,
                    request.tipoMarcacion(),
                    request.origenMarcacion(),
                    mensaje
            );
        }

        RegistroAsistencia existente = registroHoy
                .orElseThrow(() -> new BadRequestException("No existe una entrada de hoy para poder registrar la salida."));

        if (existente.getHoraSalida() != null) {
            throw new BadRequestException("La salida de hoy ya fue registrada.");
        }
        if (existente.getHoraEntrada() == null) {
            throw new BadRequestException("La entrada de hoy no esta completa.");
        }
        if (existente.getHoraAlmuerzoInicio() != null && existente.getHoraAlmuerzoFin() == null) {
            throw new BadRequestException("Debe registrar fin de almuerzo antes de cerrar la jornada.");
        }

        OffsetDateTime momentoSalida = OffsetDateTime.of(ahora, ZoneId.systemDefault().getRules().getOffset(ahora));
        existente.setHoraSalida(momentoSalida);
        existente.setTipoRegistro("SALIDA");
        existente.setInstanteServidorUltimaMarcacion(momentoSalida);
        existente.setEsFacialVerificado(Boolean.TRUE.equals(request.esFacialVerificado()) || Boolean.TRUE.equals(existente.getEsFacialVerificado()));
        if (request.precisionGpsAccuracy() != null) {
            existente.setPrecisionGpsAccuracy(request.precisionGpsAccuracy());
        }
        if (request.tokenQr() != null && !request.tokenQr().isBlank()) {
            existente.setTokenQrUtilizado(request.tokenQr());
        }
        if (request.latitud() != null) existente.setLatitud(request.latitud());
        if (request.longitud() != null) existente.setLongitud(request.longitud());
        if (request.esMockLocation() != null) existente.setEsMockLocation(request.esMockLocation());
        if (request.fotoCapturaUrl() != null) existente.setFotoCapturaUrl(request.fotoCapturaUrl());
        if (request.scoreFacialCoincidencia() != null) {
            existente.setScoreFacialCoincidencia(BigDecimal.valueOf(request.scoreFacialCoincidencia()));
        }

        RegistroAsistencia saved = registroAsistenciaRepository.save(existente);
        return new RegistroAsistenciaResponse(
                saved.getId(),
                empleado.getId(),
                empleado.getNombreCompleto(),
                saved.getFecha(),
                saved.getHoraEntrada(),
                saved.getHoraAlmuerzoInicio(),
                saved.getHoraAlmuerzoFin(),
                saved.getHoraSalida(),
                saved.getModalidadAplicada(),
                saved.getEstadoEntrada(),
                saved.getTipoRegistro(),
                saved.getInstanteServidorUltimaMarcacion(),
                saved.getEsFacialVerificado(),
                saved.getPrecisionGpsAccuracy(),
                saved.getTokenQrUtilizado(),
                saved.getLatitud(),
                saved.getLongitud(),
                saved.getEsMockLocation(),
                saved.getFotoCapturaUrl(),
                saved.getScoreFacialCoincidencia() != null ? saved.getScoreFacialCoincidencia().doubleValue() : null,
                request.tipoMarcacion(),
                request.origenMarcacion(),
                "Salida registrada correctamente"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialAsistenciaMensualResponse obtenerHistorialMensual(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new BadRequestException("El mes debe estar entre 1 y 12.");
        }
        if (anio < 2020 || anio > 2100) {
            throw new BadRequestException("El anio consultado esta fuera de rango permitido.");
        }

        UUID tenantId = requireTenant();
        Empleado empleado = requireCurrentEmployee(tenantId);
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate fechaInicio = yearMonth.atDay(1);
        LocalDate fechaFin = yearMonth.atEndOfMonth();

        List<RegistroAsistencia> registros = registroAsistenciaRepository
                .findByEmpresa_IdAndEmpleado_IdAndFechaBetween(tenantId, empleado.getId(), fechaInicio, fechaFin);

        Map<LocalDate, RegistroAsistencia> porFecha = new LinkedHashMap<>();
        for (RegistroAsistencia r : registros) {
            porFecha.putIfAbsent(r.getFecha(), r);
        }

        int totalAsistencias = 0;
        int totalRetardos = 0;
        int totalFaltas = 0;
        int totalFaltasInjustificadas = 0;
        long totalHorasTrabajadasSegundos = 0;
        long totalHorasNetasSegundos = 0;

        List<HistorialAsistenciaMensualResponse.DiaAsistenciaItem> calendario = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (int d = 1; d <= yearMonth.lengthOfMonth(); d++) {
            LocalDate fecha = yearMonth.atDay(d);
            RegistroAsistencia registro = porFecha.get(fecha);

            if (registro == null) {
                String estadoDia;
                if (fecha.isAfter(hoy)) {
                    estadoDia = "PENDIENTE";
                } else if (esFinDeSemana(fecha)) {
                    estadoDia = "NO_LABORAL";
                } else {
                    estadoDia = "SIN_REGISTRO";
                    totalFaltas++;
                }

                calendario.add(new HistorialAsistenciaMensualResponse.DiaAsistenciaItem(
                        fecha,
                        estadoDia,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        0,
                        0
                ));
                continue;
            }

            long horasTrabajadasSegundos = calcularDuracionSegundos(registro.getHoraEntrada(), registro.getHoraSalida());
            long horasAlmuerzoSegundos = calcularDuracionSegundos(registro.getHoraAlmuerzoInicio(), registro.getHoraAlmuerzoFin());
            long horasNetasSegundos = Math.max(horasTrabajadasSegundos - horasAlmuerzoSegundos, 0);

            totalHorasTrabajadasSegundos += horasTrabajadasSegundos;
            totalHorasNetasSegundos += horasNetasSegundos;

            String estadoEntrada = registro.getEstadoEntrada() != null ? registro.getEstadoEntrada().toUpperCase(Locale.ROOT) : "SIN_ESTADO";
            String estadoDia = "ASISTENCIA";
            if ("RETARDO".equalsIgnoreCase(estadoEntrada)) {
                totalRetardos++;
                estadoDia = "RETARDO";
            }
            if ("FALTA_INJUSTIFICADA".equalsIgnoreCase(estadoEntrada) || "FALTA_JUSTIFICADA".equalsIgnoreCase(estadoEntrada)) {
                totalFaltas++;
                estadoDia = "FALTA";
            }
            if ("FALTA_INJUSTIFICADA".equalsIgnoreCase(estadoEntrada)) {
                totalFaltasInjustificadas++;
            }
            if ("A_TIEMPO".equalsIgnoreCase(estadoEntrada) || "RETARDO".equalsIgnoreCase(estadoEntrada)) {
                totalAsistencias++;
            }

            calendario.add(new HistorialAsistenciaMensualResponse.DiaAsistenciaItem(
                    fecha,
                    estadoDia,
                    registro.getModalidadAplicada(),
                    registro.getEstadoEntrada(),
                    registro.getHoraEntrada(),
                    registro.getHoraAlmuerzoInicio(),
                    registro.getHoraAlmuerzoFin(),
                    registro.getHoraSalida(),
                    horasTrabajadasSegundos,
                    horasAlmuerzoSegundos,
                    horasNetasSegundos
            ));
        }

        return new HistorialAsistenciaMensualResponse(
                empleado.getId(),
                empleado.getNombreCompleto(),
                anio,
                mes,
                yearMonth.lengthOfMonth(),
                totalAsistencias,
                totalRetardos,
                totalFaltas,
                totalFaltasInjustificadas,
                totalHorasTrabajadasSegundos,
                totalHorasNetasSegundos,
                calendario
        );
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no identificado en el token JWT.");
        }
        return tenantId;
    }

    private Empleado requireCurrentEmployee(UUID tenantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }

        return empleadosRepository.findByEmailIgnoreCase(authentication.getName())
                .filter(empleado -> tenantId.equals(empleado.getEmpresaId()))
                .orElseThrow(() -> new ResourceNotFoundException("Empleado autenticado no encontrado para la empresa actual."));
    }

    private Empresa requireEmpresa(UUID tenantId) {
        return empresaRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada para la empresa autenticada."));
    }

    private Optional<RegistroAsistencia> registroHoy(UUID tenantId, UUID empleadoId, LocalDate fecha) {
        return registroAsistenciaRepository.findByEmpresa_IdAndEmpleado_IdAndFecha(tenantId, empleadoId, fecha);
    }

    private String modalidadAplicableHoy(Empleado empleado, LocalDate hoy) {
        if (empleado.getModalidadPerfil() == null) {
            return "PRESENCIAL";
        }

        if (!"HIBRIDO".equalsIgnoreCase(empleado.getModalidadPerfil())) {
            return empleado.getModalidadPerfil().toUpperCase(Locale.ROOT);
        }

        return calendarioHibridoRepository.findByEmpleado_IdAndFecha(empleado.getId(), hoy)
                .map(calendario -> calendario.getCaracterDia() != null ? calendario.getCaracterDia().toUpperCase(Locale.ROOT) : "PRESENCIAL")
                .orElse("PRESENCIAL");
    }

    private ReglaHorario obtenerRegla(UUID tenantId) {
        return reglaHorarioRepository.findByEmpresaId(tenantId).stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Debe existir al menos una regla de horario configurada para marcar asistencia."));
    }

    private String calcularEstadoLaboral(LocalTime ahora, RegistroAsistencia registro) {
        if (registro == null) {
            return "SIN_REGISTRO";
        }
        if (registro.getHoraSalida() != null) {
            return "JORNADA_FINALIZADA";
        }
        if (registro.getHoraAlmuerzoInicio() != null && registro.getHoraAlmuerzoFin() == null) {
            return "EN_ALMUERZO";
        }
        if (!ahora.isBefore(HORA_ALMUERZO_INICIO) && ahora.isBefore(HORA_ALMUERZO_FIN)) {
            return "JORNADA_ACTIVA";
        }
        return "JORNADA_ACTIVA";
    }

    private long calcularCronometroJornadaSegundos(RegistroAsistencia registro) {
        if (registro == null || registro.getHoraEntrada() == null) {
            return 0;
        }
        OffsetDateTime fin = registro.getHoraSalida() != null ? registro.getHoraSalida() : OffsetDateTime.now();
        return Math.max(Duration.between(registro.getHoraEntrada(), fin).toSeconds(), 0);
    }

    private long calcularCronometroAlmuerzoSegundos(RegistroAsistencia registro) {
        if (registro == null || registro.getHoraAlmuerzoInicio() == null) {
            return 0;
        }
        OffsetDateTime finAlmuerzo = registro.getHoraAlmuerzoFin() != null ? registro.getHoraAlmuerzoFin() : OffsetDateTime.now();
        return Math.max(Duration.between(registro.getHoraAlmuerzoInicio(), finAlmuerzo).toSeconds(), 0);
    }

    private String calcularEstadoEntrada(LocalTime horaEntrada, ReglaHorario regla) {
        LocalTime limiteRetardo = regla.getHoraEntradaOficial().plusMinutes(regla.getMinutosToleranciaRetardo());
        return horaEntrada.isAfter(limiteRetardo) ? "RETARDO" : "A_TIEMPO";
    }

    private boolean esFinDeSemana(LocalDate fecha) {
        DayOfWeek day = fecha.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private long calcularDuracionSegundos(OffsetDateTime inicio, OffsetDateTime fin) {
        if (inicio == null || fin == null) {
            return 0;
        }
        return Math.max(Duration.between(inicio, fin).toSeconds(), 0);
    }

    private String tokenQrAUsar(OrigenMarcacionAsistencia origen, String tokenQr) {
        return origen == OrigenMarcacionAsistencia.QR_DINAMICO ? tokenQr : null;
    }

    private EstadoPanelEmpleadoResponse.OffsetDateTimeResumen toRegistroHoyResumen(RegistroAsistencia registro) {
        return new EstadoPanelEmpleadoResponse.OffsetDateTimeResumen(
                registro.getId(),
                registro.getFecha(),
                registro.getHoraEntrada(),
                registro.getHoraAlmuerzoInicio(),
                registro.getHoraAlmuerzoFin(),
                registro.getHoraSalida(),
                registro.getEstadoEntrada(),
                registro.getModalidadAplicada(),
                registro.getTipoRegistro(),
                registro.getInstanteServidorUltimaMarcacion()
        );
    }

    private void validarMarcacionSeguridad(RegistrarAsistenciaRequest request, Empleado empleado, UUID tenantId, String modalidadAplicableHoy) {
        // 1. Validar origen vs modalidad
        validarOrigenVsModalidad(request.origenMarcacion(), modalidadAplicableHoy, request.tokenQr(), request.precisionGpsAccuracy());

        // 2. Validar Mock Location (GPS simulado)
        if (Boolean.TRUE.equals(request.esMockLocation())) {
            String detalles = String.format("GPS simulado detectado para empleado %s (ID: %s). Coordenadas enviadas: Lat=%s, Lon=%s. Origen: %s",
                    empleado.getNombreCompleto(), empleado.getId(), request.latitud(), request.longitud(), request.origenMarcacion());
            alertaService.registrarYDispararAlerta(empleado, "MOCK_LOCATION_DETECTADA", detalles);
            throw new BadRequestException("No se permiten ubicaciones simuladas o alteradas.");
        }

        // 3. Validar QR Dinámico
        if ("PRESENCIAL".equalsIgnoreCase(modalidadAplicableHoy) && request.origenMarcacion() == OrigenMarcacionAsistencia.QR_DINAMICO) {
            qrValidationService.validarQrDinamico(request.tokenQr(), tenantId);
        }

        // 4. Validar Biometría y Geolocalización para modalidad REMOTO
        if ("REMOTO".equalsIgnoreCase(modalidadAplicableHoy)) {
            // A. Validación Biométrica Facial
            if (request.fotoCapturaUrl() == null || request.fotoCapturaUrl().isBlank()) {
                throw new BadRequestException("La modalidad remota requiere una captura fotográfica.");
            }
            if (!Boolean.TRUE.equals(request.esFacialVerificado()) || request.scoreFacialCoincidencia() == null || request.scoreFacialCoincidencia() < 80.0) {
                String detalles = String.format("Falla de coincidencia facial para empleado %s (ID: %s). EsFacialVerificado: %s, Score Coincidencia: %s. URL Captura: %s",
                        empleado.getNombreCompleto(), empleado.getId(), request.esFacialVerificado(), request.scoreFacialCoincidencia(), request.fotoCapturaUrl());
                alertaService.registrarYDispararAlerta(empleado, "FACE_MISMATCH", detalles);
                throw new BadRequestException("Falla de verificación biométrica. El rostro no coincide.");
            }

            // B. Validación de Geolocalización (Geocercas)
            if (request.latitud() == null || request.longitud() == null) {
                throw new BadRequestException("La modalidad remota requiere coordenadas GPS.");
            }

            List<com.proyecto.version1.Features.GeocercasRemota.GeocercasRemota> geocercas =
                    geocercasRemotaRepository.findByEmpleado_IdAndEmpleado_EmpresaId(empleado.getId(), tenantId);

            if (geocercas.isEmpty()) {
                String detalles = String.format("El empleado %s (ID: %s) intentó marcar asistencia remota sin geocercas configuradas. Coordenadas enviadas: Lat=%s, Lon=%s",
                        empleado.getNombreCompleto(), empleado.getId(), request.latitud(), request.longitud());
                alertaService.registrarYDispararAlerta(empleado, "FUERA_DE_GEOCERCA", detalles);
                throw new BadRequestException("El empleado no tiene ninguna geocerca remota configurada.");
            }

            boolean dentroDeAlgunaGeocerca = false;
            for (com.proyecto.version1.Features.GeocercasRemota.GeocercasRemota geocerca : geocercas) {
                double distancia = GeoUtils.calcularDistanciaMetros(
                        request.latitud(), request.longitud(), geocerca.getLatitud(), geocerca.getLongitud());
                if (distancia <= geocerca.getRadioToleranciaMetros()) {
                    dentroDeAlgunaGeocerca = true;
                    break;
                }
            }

            if (!dentroDeAlgunaGeocerca) {
                String detalles = String.format("Empleado %s (ID: %s) fuera del radio de sus geocercas permitidas. Coordenadas enviadas: Lat=%s, Lon=%s",
                        empleado.getNombreCompleto(), empleado.getId(), request.latitud(), request.longitud());
                alertaService.registrarYDispararAlerta(empleado, "FUERA_DE_GEOCERCA", detalles);
                throw new BadRequestException("El dispositivo se encuentra fuera de la geocerca permitida.");
            }
        }
    }

    private void validarOrigenVsModalidad(
            OrigenMarcacionAsistencia origen,
            String modalidadAplicableHoy,
            String tokenQr,
            BigDecimal precisionGpsAccuracy) {

        if ("PRESENCIAL".equalsIgnoreCase(modalidadAplicableHoy)) {
            if (origen != OrigenMarcacionAsistencia.QR_FISICO && origen != OrigenMarcacionAsistencia.QR_DINAMICO) {
                throw new BadRequestException("La modalidad presencial debe registrarse mediante QR fisico o dinamico.");
            }
            if (origen == OrigenMarcacionAsistencia.QR_DINAMICO && (tokenQr == null || tokenQr.isBlank())) {
                throw new BadRequestException("Debe enviar un token QR dinamico valido.");
            }
            return;
        }

        if ("REMOTO".equalsIgnoreCase(modalidadAplicableHoy)) {
            if (origen != OrigenMarcacionAsistencia.BOTON_REMOTO) {
                throw new BadRequestException("La modalidad remota debe registrarse desde el boton de asistencia remota.");
            }
            if (precisionGpsAccuracy == null) {
                throw new BadRequestException("La modalidad remota requiere precision GPS.");
            }
            return;
        }

        throw new BadRequestException("La modalidad del dia no permite marcacion en este momento.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaResponse> consultarMisGeocercas() {
        UUID tenantId = requireTenant();
        Empleado empleado = requireCurrentEmployee(tenantId);
        
        List<com.proyecto.version1.Features.GeocercasRemota.GeocercasRemota> geocercas =
                geocercasRemotaRepository.findByEmpleado_IdAndEmpleado_EmpresaId(empleado.getId(), tenantId);
                
        List<com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaResponse> response = new ArrayList<>();
        for (com.proyecto.version1.Features.GeocercasRemota.GeocercasRemota g : geocercas) {
            response.add(new com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaResponse(
                    g.getId(),
                    g.getEmpleado().getId(),
                    g.getDescripcion(),
                    g.getLatitud(),
                    g.getLongitud(),
                    g.getRadioToleranciaMetros()
            ));
        }
        return response;
    }
}

