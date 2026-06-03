package com.proyecto.version1.Features.Reportes_Prenomina.service.impl;

import com.proyecto.version1.Features.ContratosEmpleados.ContratosEmpleado;
import com.proyecto.version1.Features.ContratosEmpleados.ContratosEmpleadoRepository;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Empresas.Empresa;
import com.proyecto.version1.Features.Empresas.repository.EmpresaRepository;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.Features.RecargosConfiguracion.ConfiguracionRecargosEmpresa;
import com.proyecto.version1.Features.RecargosConfiguracion.ConfiguracionRecargosEmpresaRepository;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorario;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorarioRepository;
import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import com.proyecto.version1.Features.Reportes_Prenomina.ReportesPrenominaMensual;
import com.proyecto.version1.Features.Reportes_Prenomina.ReportesPrenominaMensualRepository;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReporteConsolidadoResponse;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReporteGeneracionRequest;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReportePrenominaFilterRequest;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReportesPrenominaMensualResponse;
import com.proyecto.version1.Features.Reportes_Prenomina.mapper.ReportesPrenominaMensualMapper;
import com.proyecto.version1.Features.Reportes_Prenomina.service.ReportesPrenominaMensualService;
import com.proyecto.version1.Features.Reportes_Prenomina.util.ColombiaFestivosUtils;
import com.proyecto.version1.Features.Vacaciones.SolicitudesVacacione;
import com.proyecto.version1.Features.Vacaciones.repository.SolicitudesVacacionesRepository;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportesPrenominaMensualServiceImpl implements ReportesPrenominaMensualService {

    private static final String ESTADO_BORRADOR = "BORRADOR";
    private static final int HORAS_MENSUALES_ESTATICAS = 240;
    private static final LocalTime INICIO_DIURNO = LocalTime.of(6, 0);
    private static final LocalTime FIN_DIURNO = LocalTime.of(21, 0);

    private final ReportesPrenominaMensualRepository reportesRepository;
    private final EmpleadosRepository empleadosRepository;
    private final EmpresaRepository empresaRepository;
    private final RegistroAsistenciaRepository registroAsistenciaRepository;
    private final ContratosEmpleadoRepository contratosEmpleadoRepository;
    private final ConfiguracionRecargosEmpresaRepository configuracionRecargosRepository;
    private final ReglaHorarioRepository reglaHorarioRepository;
    private final SolicitudesVacacionesRepository solicitudesVacacionesRepository;
    private final ReportesPrenominaMensualMapper mapper;

    @Override
    public List<ReportesPrenominaMensualResponse> generar(ReporteGeneracionRequest request) {
        UUID tenantId = requireTenant();
        validarRango(request.fechaInicio(), request.fechaFin());
        List<ResumenCalculado> calculados = calcularResumenes(tenantId, request);

        List<ReportesPrenominaMensual> guardados = new ArrayList<>();
        for (ResumenCalculado calculado : calculados) {
            ReportesPrenominaMensual entity = reportesRepository
                    .findByEmpresa_IdAndAnioPeriodoAndMesPeriodo(tenantId, calculado.yearMonth.getYear(), calculado.yearMonth.getMonthValue())
                    .stream()
                    .filter(r -> r.getEmpleado().getId().equals(calculado.empleado.getId()))
                    .findFirst()
                    .orElseGet(ReportesPrenominaMensual::new);

            entity.setEmpresa(calculado.empresa);
            entity.setEmpleado(calculado.empleado);
            entity.setMesPeriodo(calculado.yearMonth.getMonthValue());
            entity.setAnioPeriodo(calculado.yearMonth.getYear());
            entity.setDiasTrabajadosEfectivos(calculado.numerico().diasTrabajadosEfectivos());
            entity.setDiasFaltaInjustificada(calculado.numerico().diasFaltaInjustificada());
            entity.setHorasExtrasDiurnasTotales(calculado.numerico().horasExtrasDiurnasTotales());
            entity.setHorasExtrasNocturnasTotales(calculado.numerico().horasExtrasNocturnasTotales());
            entity.setMontoSalarioBaseProporcional(calculado.numerico().montoSalarioBaseProporcional());
            entity.setMontoGananciaExtras(calculado.numerico().montoGananciaExtras());
            entity.setMontoDeduccionesFaltas(calculado.numerico().montoDeduccionesFaltas());
            entity.setMontoNetoPagar(calculado.numerico().montoNetoPagar());
            entity.setEstadoReporte(ESTADO_BORRADOR);
            entity.setRequiereRecalculo(false);
            guardados.add(reportesRepository.save(entity));
        }

        return guardados.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportesPrenominaMensualResponse> listar(ReportePrenominaFilterRequest filter, Pageable pageable) {
        UUID tenantId = requireTenant();
        validarRango(filter.fechaInicio(), filter.fechaFin());

        List<ReportesPrenominaMensual> all = reportesRepository.findConsolidadoPorRango(
                tenantId,
                filter.fechaInicio().getYear(),
                filter.fechaInicio().getMonthValue(),
                filter.fechaFin().getYear(),
                filter.fechaFin().getMonthValue()
        );

        List<ReportesPrenominaMensual> filtrados = all.stream()
                .filter(r -> filter.estadoReporte() == null || filter.estadoReporte().isBlank() || filter.estadoReporte().equalsIgnoreCase(r.getEstadoReporte()))
                .filter(r -> filter.empleadoId() == null || filter.empleadoId().equals(r.getEmpleado().getId()))
                .toList();

        return toPageResponse(filtrados, pageable, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportesPrenominaMensualResponse obtener(UUID id) {
        UUID tenantId = requireTenant();
        ReportesPrenominaMensual entity = reportesRepository.findByIdAndEmpresa_Id(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte de pre-nómina no encontrado con ID: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteConsolidadoResponse> consolidar(ReporteGeneracionRequest request) {
        UUID tenantId = requireTenant();
        validarRango(request.fechaInicio(), request.fechaFin());
        return calcularResumenes(tenantId, request).stream()
                .map(resumen -> resumen.toConsolidado(mapper))
                .sorted(Comparator.comparing(ReporteConsolidadoResponse::empleadoNombre))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarCsv(ReporteGeneracionRequest request) {
        List<ReporteConsolidadoResponse> data = consolidar(request);
        StringBuilder sb = new StringBuilder();
        sb.append("empleado_id;empleado_nombre;tipo_contrato;tipo_moneda;mes_periodo;anio_periodo;fecha_inicio;fecha_fin;dias_trabajados;dias_falta_injustificada;dias_vacaciones;llegadas_tardias;horas_trabajadas;horas_extras_diurnas;horas_extras_nocturnas;monto_salario_base_proporcional;monto_ganancia_extras;monto_deducciones_faltas;monto_neto_pagar\n");
        for (ReporteConsolidadoResponse row : data) {
            sb.append(row.empleadoId()).append(';')
                    .append(escapeCsv(row.empleadoNombre())).append(';')
                    .append(escapeCsv(row.tipoContrato())).append(';')
                    .append(escapeCsv(row.tipoMoneda())).append(';')
                    .append(row.mesPeriodo()).append(';')
                    .append(row.anioPeriodo()).append(';')
                    .append(row.fechaInicio()).append(';')
                    .append(row.fechaFin()).append(';')
                    .append(row.diasTrabajadosEfectivos()).append(';')
                    .append(row.diasFaltaInjustificada()).append(';')
                    .append(row.diasVacacionesAprobadas()).append(';')
                    .append(row.llegadasTardias()).append(';')
                    .append(row.horasTrabajadasTotales()).append(';')
                    .append(row.horasExtrasDiurnasTotales()).append(';')
                    .append(row.horasExtrasNocturnasTotales()).append(';')
                    .append(row.montoSalarioBaseProporcional()).append(';')
                    .append(row.montoGananciaExtras()).append(';')
                    .append(row.montoDeduccionesFaltas()).append(';')
                    .append(row.montoNetoPagar()).append('\n');
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(ReporteGeneracionRequest request) {
        List<ReporteConsolidadoResponse> data = consolidar(request);
        try {
            return generarXlsx(data);
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible generar el archivo Excel de pre-nómina", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(ReporteGeneracionRequest request) {
        List<ReporteConsolidadoResponse> data = consolidar(request);
        try {
            return generarPdf(data);
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible generar el PDF de pre-nómina", ex);
        }
    }

    private List<ResumenCalculado> calcularResumenes(UUID empresaId, ReporteGeneracionRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada para el tenant autenticado."));
        List<RegistroAsistencia> registros = obtenerRegistros(empresaId, request);
        List<SolicitudesVacacione> vacaciones = solicitudesVacacionesRepository
                .findByEmpleado_EmpresaIdAndEstadoSolicitudAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        empresaId,
                        "APROBADO",
                        request.fechaFin(),
                        request.fechaInicio());

        Map<YearMonth, Map<UUID, Integer>> vacacionesPorMes = calcularVacacionesPorMes(vacaciones, request.fechaInicio(), request.fechaFin());

        Set<UUID> empleadosObjetivo = new LinkedHashSet<>();
        if (request.empleadoId() != null) {
            empleadosObjetivo.add(obtenerEmpleadoValidado(request.empleadoId(), empresaId).getId());
        } else {
            empleadosObjetivo.addAll(registros.stream().map(r -> r.getEmpleado().getId()).filter(Objects::nonNull).toList());
            empleadosObjetivo.addAll(vacaciones.stream().map(v -> v.getEmpleado().getId()).filter(Objects::nonNull).toList());
            if (empleadosObjetivo.isEmpty()) {
                empleadosObjetivo.addAll(empleadosRepository.findByEmpresaIdAndActivoTrue(empresaId).stream().map(Empleado::getId).toList());
            }
        }

        List<ResumenCalculado> resumenes = new ArrayList<>();
        for (UUID empleadoId : empleadosObjetivo) {
            Empleado empleado = obtenerEmpleadoValidado(empleadoId, empresaId);
            List<RegistroAsistencia> registrosEmpleado = registros.stream()
                    .filter(r -> r.getEmpleado() != null && empleadoId.equals(r.getEmpleado().getId()))
                    .toList();
            if (registrosEmpleado.isEmpty() && !tieneVacacionesEnRango(vacaciones, empleadoId, request.fechaInicio(), request.fechaFin()) && request.empleadoId() == null) {
                continue;
            }

            Map<YearMonth, List<RegistroAsistencia>> porMes = registrosEmpleado.stream()
                    .collect(Collectors.groupingBy(r -> YearMonth.from(r.getFecha()), LinkedHashMap::new, Collectors.toList()));

            Set<YearMonth> periodos = new LinkedHashSet<>();
            periodos.addAll(porMes.keySet());
            periodos.addAll(vacacionesPorMes.keySet().stream()
                    .filter(ym -> vacacionesPorMes.getOrDefault(ym, Map.of()).containsKey(empleadoId))
                    .toList());
            if (periodos.isEmpty() && request.empleadoId() != null) {
                periodos.add(YearMonth.from(request.fechaInicio()));
            }

            for (YearMonth yearMonth : periodos) {
                LocalDate monthStart = yearMonth.atDay(1);
                LocalDate monthEnd = yearMonth.atEndOfMonth();
                LocalDate effectiveStart = maxDate(request.fechaInicio(), monthStart);
                LocalDate effectiveEnd = minDate(request.fechaFin(), monthEnd);
                if (effectiveStart.isAfter(effectiveEnd)) {
                    continue;
                }

                List<RegistroAsistencia> registrosMes = porMes.getOrDefault(yearMonth, List.of());
                int vacacionesDias = vacacionesPorMes.getOrDefault(yearMonth, Map.of()).getOrDefault(empleadoId, 0);
                ContratosEmpleado contrato = obtenerContratoVigente(empleadoId, effectiveStart);
                ConfiguracionRecargosEmpresa recargos = obtenerRecargos(empresaId);
                ReglaHorario reglaHorario = obtenerRegla(empresaId);

                ResumenNumerico numerico = calcularNumerico(contrato, recargos, reglaHorario, registrosMes, vacacionesDias);
                resumenes.add(new ResumenCalculado(
                        empresa,
                        empleado,
                        contrato,
                        yearMonth,
                        effectiveStart,
                        effectiveEnd,
                        numerico
                ));
            }
        }

        return resumenes.stream()
                .sorted(Comparator.comparing((ResumenCalculado r) -> r.yearMonth).thenComparing(r -> r.empleado.getNombreCompleto()))
                .toList();
    }

    private ResumenNumerico calcularNumerico(ContratosEmpleado contrato, ConfiguracionRecargosEmpresa recargos,
                                             ReglaHorario reglaHorario, List<RegistroAsistencia> registrosMes, int vacacionesDias) {
        BigDecimal salarioMensual = contrato.getSalarioBaseMensual();
        BigDecimal salarioDiario = salarioMensual.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        BigDecimal salarioHora = salarioMensual.divide(BigDecimal.valueOf(HORAS_MENSUALES_ESTATICAS), 4, RoundingMode.HALF_UP);

        int diasTrabajados = 0;
        int faltasInjustificadas = 0;
        int llegadasTardias = 0;
        long minutosTardanza = 0;
        BigDecimal horasTrabajadas = BigDecimal.ZERO;
        BigDecimal horasExtrasDiurnas = BigDecimal.ZERO;
        BigDecimal horasExtrasNocturnas = BigDecimal.ZERO;
        BigDecimal montoGananciaExtras = BigDecimal.ZERO;

        Map<LocalDate, RegistroAsistencia> unicosPorFecha = registrosMes.stream()
                .collect(Collectors.toMap(RegistroAsistencia::getFecha, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        for (RegistroAsistencia registro : unicosPorFecha.values()) {
            if (registro.getEstadoEntrada() == null) {
                continue;
            }

            boolean tieneTiempo = registro.getHoraEntrada() != null && registro.getHoraSalida() != null;
            if ("A_TIEMPO".equalsIgnoreCase(registro.getEstadoEntrada()) || "RETARDO".equalsIgnoreCase(registro.getEstadoEntrada())) {
                diasTrabajados++;
            }
            if ("FALTA_INJUSTIFICADA".equalsIgnoreCase(registro.getEstadoEntrada())) {
                faltasInjustificadas++;
            }
            if ("RETARDO".equalsIgnoreCase(registro.getEstadoEntrada())) {
                llegadasTardias++;
            }

            if (tieneTiempo) {
                BigDecimal horasDia = BigDecimal.valueOf(Duration.between(registro.getHoraEntrada(), registro.getHoraSalida()).toMinutes())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                horasTrabajadas = horasTrabajadas.add(horasDia);

                TiemposExtra extras = calcularHorasExtra(registro, reglaHorario);
                horasExtrasDiurnas = horasExtrasDiurnas.add(extras.horasDiurnas);
                horasExtrasNocturnas = horasExtrasNocturnas.add(extras.horasNocturnas);
                minutosTardanza += extras.minutosTardanza;

                BigDecimal factorDiurno = ColombiaFestivosUtils.esDominicalOFestivo(registro.getFecha())
                        ? recargos.getFactorHoraDominicalFestiva()
                        : recargos.getFactorHoraExtraDiurna();
                BigDecimal factorNocturno = ColombiaFestivosUtils.esDominicalOFestivo(registro.getFecha())
                        ? recargos.getFactorHoraDominicalFestiva()
                        : recargos.getFactorHoraExtraNocturna();
                montoGananciaExtras = montoGananciaExtras
                        .add(calcularMontoExtras(extras.horasDiurnas, salarioHora, factorDiurno))
                        .add(calcularMontoExtras(extras.horasNocturnas, salarioHora, factorNocturno));
            }
        }

        BigDecimal diasBasePagables = BigDecimal.valueOf(diasTrabajados + vacacionesDias);
        BigDecimal montoSalarioBaseProporcional = salarioDiario.multiply(diasBasePagables).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoDeduccionesFaltas = salarioDiario.multiply(BigDecimal.valueOf(faltasInjustificadas)).setScale(2, RoundingMode.HALF_UP)
                .add(recargos.getMultaRetardoPorMinuto().multiply(BigDecimal.valueOf(minutosTardanza)).setScale(2, RoundingMode.HALF_UP));

        montoGananciaExtras = montoGananciaExtras.setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoNetoPagar = montoSalarioBaseProporcional.add(montoGananciaExtras).subtract(montoDeduccionesFaltas).setScale(2, RoundingMode.HALF_UP);

        return new ResumenNumerico(
                diasTrabajados,
                faltasInjustificadas,
                llegadasTardias,
                vacacionesDias,
                minutosTardanza,
                horasTrabajadas.setScale(2, RoundingMode.HALF_UP),
                horasExtrasDiurnas.setScale(2, RoundingMode.HALF_UP),
                horasExtrasNocturnas.setScale(2, RoundingMode.HALF_UP),
                montoSalarioBaseProporcional,
                montoGananciaExtras,
                montoDeduccionesFaltas,
                montoNetoPagar
        );
    }

    private TiemposExtra calcularHorasExtra(RegistroAsistencia registro, ReglaHorario reglaHorario) {
        LocalDateTime entradaOficial = registro.getFecha().atTime(reglaHorario.getHoraEntradaOficial());
        LocalDateTime salidaOficial = registro.getFecha().atTime(reglaHorario.getHoraSalidaOficial());
        if (salidaOficial.isBefore(entradaOficial) || salidaOficial.equals(entradaOficial)) {
            salidaOficial = salidaOficial.plusDays(1);
        }

        LocalDateTime salidaReal = registro.getHoraSalida().toLocalDateTime();
        if (!salidaReal.isAfter(salidaOficial)) {
            return new TiemposExtra(BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }

        LocalDateTime inicioExtra = salidaOficial;
        BigDecimal horasDiurnas = BigDecimal.ZERO;
        BigDecimal horasNocturnas = BigDecimal.ZERO;
        long minutosTardanza = 0;

        if (registro.getHoraEntrada() != null) {
            LocalDateTime entradaReal = registro.getHoraEntrada().toLocalDateTime();
            if (entradaReal.isAfter(entradaOficial)) {
                minutosTardanza = Duration.between(entradaOficial, entradaReal).toMinutes();
            }
        }

        LocalDateTime cursor = inicioExtra;
        while (cursor.isBefore(salidaReal)) {
            LocalDateTime nextBoundary = nextBoundary(cursor, salidaReal);
            long minutos = Duration.between(cursor, nextBoundary).toMinutes();
            if (minutos > 0) {
                if (esDiurno(cursor.toLocalTime())) {
                    horasDiurnas = horasDiurnas.add(BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
                } else {
                    horasNocturnas = horasNocturnas.add(BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
                }
            }
            cursor = nextBoundary;
        }

        return new TiemposExtra(horasDiurnas, horasNocturnas, minutosTardanza);
    }

    private BigDecimal calcularMontoExtras(BigDecimal horas, BigDecimal salarioHora, BigDecimal factor) {
        if (horas.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return horas.multiply(salarioHora).multiply(factor.subtract(BigDecimal.ONE)).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean esDiurno(LocalTime time) {
        return !time.isBefore(INICIO_DIURNO) && time.isBefore(FIN_DIURNO);
    }

    private LocalDateTime nextBoundary(LocalDateTime current, LocalDateTime max) {
        LocalDateTime midnightNext = current.toLocalDate().plusDays(1).atStartOfDay();
        LocalDateTime six = current.toLocalDate().atTime(INICIO_DIURNO);
        LocalDateTime nine = current.toLocalDate().atTime(FIN_DIURNO);
        List<LocalDateTime> boundaries = java.util.stream.Stream.of(midnightNext, six, nine).filter(b -> b.isAfter(current)).sorted().toList();
        LocalDateTime candidate = boundaries.isEmpty() ? max : boundaries.getFirst();
        return candidate.isBefore(max) ? candidate : max;
    }

    private ConfiguracionRecargosEmpresa obtenerRecargos(UUID empresaId) {
        return configuracionRecargosRepository.findByEmpresa_Id(empresaId)
                .orElseGet(() -> ConfiguracionRecargosEmpresa.builder()
                        .factorHoraExtraDiurna(new BigDecimal("1.25"))
                        .factorHoraExtraNocturna(new BigDecimal("1.75"))
                        .factorHoraDominicalFestiva(new BigDecimal("2.00"))
                        .multaRetardoPorMinuto(BigDecimal.ZERO)
                        .build());
    }

    private ReglaHorario obtenerRegla(UUID empresaId) {
        List<ReglaHorario> reglas = reglaHorarioRepository.findByEmpresaId(empresaId);
        if (reglas.isEmpty()) {
            throw new BadRequestException("Debe existir al menos una regla de horario configurada para generar la pre-nómina");
        }
        return reglas.getFirst();
    }

    private ContratosEmpleado obtenerContratoVigente(UUID empleadoId, LocalDate fechaReferencia) {
        return contratosEmpleadoRepository
                .findTopByEmpleado_IdAndActivoTrueAndFechaIngresoLessThanEqualAndFechaRetiroIsNullOrderByFechaIngresoDesc(empleadoId, fechaReferencia)
                .or(() -> contratosEmpleadoRepository.findTopByEmpleado_IdAndActivoTrueAndFechaIngresoLessThanEqualAndFechaRetiroGreaterThanEqualOrderByFechaIngresoDesc(empleadoId, fechaReferencia, fechaReferencia))
                .or(() -> contratosEmpleadoRepository.findTopByEmpleado_IdAndActivoTrueAndFechaIngresoLessThanEqualOrderByFechaIngresoDesc(empleadoId, fechaReferencia))
                .orElseThrow(() -> new BadRequestException("No existe un contrato activo para el empleado en la fecha " + fechaReferencia));
    }

    private List<RegistroAsistencia> obtenerRegistros(UUID empresaId, ReporteGeneracionRequest request) {
        if (request.empleadoId() != null) {
            Empleado empleado = obtenerEmpleadoValidado(request.empleadoId(), empresaId);
            return registroAsistenciaRepository.findByEmpresa_IdAndEmpleado_IdAndFechaBetween(empresaId, empleado.getId(), request.fechaInicio(), request.fechaFin());
        }
        return registroAsistenciaRepository.findByEmpresa_IdAndFechaBetween(empresaId, request.fechaInicio(), request.fechaFin());
    }

    private Empleado obtenerEmpleadoValidado(UUID empleadoId, UUID empresaId) {
        return empleadosRepository.findByIdAndEmpresaId(empleadoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada: " + empleadoId));
    }

    private Map<YearMonth, Map<UUID, Integer>> calcularVacacionesPorMes(List<SolicitudesVacacione> vacaciones, LocalDate inicio, LocalDate fin) {
        Map<YearMonth, Map<UUID, Integer>> resultado = new HashMap<>();
        for (SolicitudesVacacione solicitud : vacaciones) {
            LocalDate current = maxDate(solicitud.getFechaInicio(), inicio);
            LocalDate end = minDate(solicitud.getFechaFin(), fin);
            while (!current.isAfter(end)) {
                YearMonth ym = YearMonth.from(current);
                resultado.computeIfAbsent(ym, k -> new HashMap<>())
                        .merge(solicitud.getEmpleado().getId(), 1, Integer::sum);
                current = current.plusDays(1);
            }
        }
        return resultado;
    }

    private boolean tieneVacacionesEnRango(List<SolicitudesVacacione> vacaciones, UUID empleadoId, LocalDate inicio, LocalDate fin) {
        return vacaciones.stream().anyMatch(v -> empleadoId.equals(v.getEmpleado().getId()) && !v.getFechaInicio().isAfter(fin) && !v.getFechaFin().isBefore(inicio));
    }

    private void validarRango(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new BadRequestException("Las fechas del reporte son requeridas");
        }
        if (inicio.isAfter(fin)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no identificado en token JWT.");
        }
        return tenantId;
    }

    private <T, R> PageResponse<R> toPageResponse(List<T> source, Pageable pageable, Function<T, R> mapperFn) {
        int fromIndex = Math.min((int) pageable.getOffset(), source.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), source.size());
        List<R> content = source.subList(fromIndex, toIndex).stream().map(mapperFn).toList();
        int totalPages = pageable.getPageSize() == 0 ? 1 : (int) Math.ceil((double) source.size() / pageable.getPageSize());
        return new PageResponse<>(content, pageable.getPageNumber(), pageable.getPageSize(), source.size(), totalPages);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(";", ",").replace("\n", " ").replace("\r", " ");
    }

    private String sanitizePdfText(String text) {
        if (text == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replace('–', '-').replace('—', '-');
    }

    private LocalDate maxDate(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDate minDate(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }

    private byte[] generarXlsx(List<ReporteConsolidadoResponse> data) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(out)) {

            escribirZip(zos, "[Content_Types].xml", contentTypesXml());
            escribirZip(zos, "_rels/.rels", rootRelsXml());
            escribirZip(zos, "xl/workbook.xml", workbookXml());
            escribirZip(zos, "xl/_rels/workbook.xml.rels", workbookRelsXml());
            escribirZip(zos, "xl/worksheets/sheet1.xml", sheetXml(data));

            zos.finish();
            return out.toByteArray();
        }
    }

    private byte[] generarPdf(List<ReporteConsolidadoResponse> data) throws IOException {
        List<List<String>> pages = new ArrayList<>();
        List<String> current = new ArrayList<>();
        current.add("Reporte de Pre-Nomina Consolidado");
        current.add("Empleado | Contrato | Mes/Año | Trabajados | Faltas | Vacaciones | Tardanzas | Neto");
        for (ReporteConsolidadoResponse row : data) {
            String line = String.format(Locale.ROOT,
                    "%s | %s | %02d/%d | %d | %d | %d | %d | %s",
                    row.empleadoNombre(),
                    row.tipoContrato(),
                    row.mesPeriodo(),
                    row.anioPeriodo(),
                    row.diasTrabajadosEfectivos(),
                    row.diasFaltaInjustificada(),
                    row.diasVacacionesAprobadas(),
                    row.llegadasTardias(),
                    row.montoNetoPagar());
            current.add(line);
            if (current.size() >= 38) {
                pages.add(current);
                current = new ArrayList<>();
                current.add("Reporte de Pre-Nomina Consolidado (continuacion)");
            }
        }
        if (!current.isEmpty()) {
            pages.add(current);
        }

        List<byte[]> objects = new ArrayList<>();
        objects.add(null); // índice 0 no usado
        objects.add(pdfObject(1, "<< /Type /Catalog /Pages 2 0 R >>"));

        StringBuilder pagesKids = new StringBuilder();
        int nextObject = 4;
        List<String> pageObjects = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            int pageObj = nextObject++;
            int contentObj = nextObject++;
            pagesKids.append(pageObj).append(" 0 R ");
            pageObjects.add(buildPageObject(pageObj, contentObj));
            pageObjects.add(buildContentObject(contentObj, pages.get(i)));
        }

        objects.add(pdfObject(2, "<< /Type /Pages /Kids [ " + pagesKids + "] /Count " + pages.size() + " >>"));
        objects.add(pdfObject(3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));

        // Agregar páginas y contenidos
        for (String obj : pageObjects) {
            int objNum = Integer.parseInt(obj.substring(0, obj.indexOf('|')));
            String body = obj.substring(obj.indexOf('|') + 1);
            while (objects.size() <= objNum) {
                objects.add(null);
            }
            objects.set(objNum, pdfObject(objNum, body));
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write("%PDF-1.4\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            List<Integer> offsets = new ArrayList<>();
            offsets.add(0);
            for (int i = 1; i < objects.size(); i++) {
                byte[] obj = objects.get(i);
                if (obj == null) {
                    continue;
                }
                offsets.add(out.size());
                out.write(obj);
            }
            int xrefStart = out.size();
            out.write(String.format(java.util.Locale.ROOT, "xref\n0 %d\n", objects.size()).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            out.write("0000000000 65535 f \n".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            for (int i = 1; i < objects.size(); i++) {
                byte[] obj = objects.get(i);
                if (obj == null) {
                    out.write("0000000000 00000 f \n".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                } else {
                    int offset = offsets.get(i);
                    out.write(String.format(java.util.Locale.ROOT, "%010d 00000 n \n", offset).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                }
            }
            out.write(String.format(java.util.Locale.ROOT, "trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF", objects.size(), xrefStart).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            return out.toByteArray();
        }
    }

    private String contentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "</Types>";
    }

    private String rootRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private String workbookXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"PreNomina\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>";
    }

    private String workbookRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "</Relationships>";
    }

    private String sheetXml(List<ReporteConsolidadoResponse> data) {
        String[] headers = {"Empleado", "Contrato", "Mes", "Año", "Días trabajados", "Faltas injustificadas", "Vacaciones", "Llegadas tardías", "Horas trabajadas", "Horas extra diurnas", "Horas extra nocturnas", "Base proporcional", "Ganancia extras", "Deducciones", "Neto pagar"};
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        appendRow(xml, 1, List.of(headers));
        int rowNum = 2;
        for (ReporteConsolidadoResponse row : data) {
            appendRow(xml, rowNum++, List.of(
                    row.empleadoNombre(),
                    row.tipoContrato(),
                    String.valueOf(row.mesPeriodo()),
                    String.valueOf(row.anioPeriodo()),
                    String.valueOf(row.diasTrabajadosEfectivos()),
                    String.valueOf(row.diasFaltaInjustificada()),
                    String.valueOf(row.diasVacacionesAprobadas()),
                    String.valueOf(row.llegadasTardias()),
                    row.horasTrabajadasTotales().toPlainString(),
                    row.horasExtrasDiurnasTotales().toPlainString(),
                    row.horasExtrasNocturnasTotales().toPlainString(),
                    row.montoSalarioBaseProporcional().toPlainString(),
                    row.montoGananciaExtras().toPlainString(),
                    row.montoDeduccionesFaltas().toPlainString(),
                    row.montoNetoPagar().toPlainString()
            ));
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private void appendRow(StringBuilder xml, int rowNum, List<String> values) {
        xml.append("<row r=\"").append(rowNum).append("\">");
        for (int i = 0; i < values.size(); i++) {
            xml.append("<c r=\"").append(colName(i)).append(rowNum).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                    .append(xmlEscape(values.get(i)))
                    .append("</t></is></c>");
        }
        xml.append("</row>");
    }

    private String colName(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index;
        do {
            sb.insert(0, (char) ('A' + (n % 26)));
            n = (n / 26) - 1;
        } while (n >= 0);
        return sb.toString();
    }

    private String xmlEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private void escribirZip(java.util.zip.ZipOutputStream zos, String path, String content) throws IOException {
        zos.putNextEntry(new java.util.zip.ZipEntry(path));
        zos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private byte[] pdfObject(int number, String body) {
        return String.format(java.util.Locale.ROOT, "%d 0 obj\n%s\nendobj\n", number, body).getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    }

    private String buildPageObject(int pageObj, int contentObj) {
        return pageObj + "|<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 3 0 R >> >> /Contents " + contentObj + " 0 R >>";
    }

    private String buildContentObject(int contentObj, List<String> lines) {
        StringBuilder stream = new StringBuilder();
        stream.append("BT\n/F1 12 Tf\n50 800 Td\n");
        for (int i = 0; i < lines.size(); i++) {
            String line = sanitizePdfText(lines.get(i));
            if (i == 0) {
                stream.append("(").append(escapePdfText(line)).append(") Tj\n");
                stream.append("/F1 8 Tf\n");
            } else {
                stream.append("0 -14 Td\n(").append(escapePdfText(line)).append(") Tj\n");
            }
        }
        stream.append("ET");
        byte[] bytes = stream.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        return contentObj + "|<< /Length " + bytes.length + " >>\nstream\n" + stream + "\nendstream";
    }

    private String escapePdfText(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private record ResumenNumerico(
            int diasTrabajadosEfectivos,
            int diasFaltaInjustificada,
            int llegadasTardias,
            int diasVacacionesAprobadas,
            long minutosTardanza,
            BigDecimal horasTrabajadasTotales,
            BigDecimal horasExtrasDiurnasTotales,
            BigDecimal horasExtrasNocturnasTotales,
            BigDecimal montoSalarioBaseProporcional,
            BigDecimal montoGananciaExtras,
            BigDecimal montoDeduccionesFaltas,
            BigDecimal montoNetoPagar
    ) {}

    private record TiemposExtra(BigDecimal horasDiurnas, BigDecimal horasNocturnas, long minutosTardanza) {}

    private record ResumenCalculado(
            Empresa empresa,
            Empleado empleado,
            ContratosEmpleado contrato,
            YearMonth yearMonth,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            ResumenNumerico numerico
    ) {
        private ReporteConsolidadoResponse toConsolidado(ReportesPrenominaMensualMapper mapper) {
            return mapper.toConsolidado(
                    ReportesPrenominaMensual.builder()
                            .empresa(empresa)
                            .empleado(empleado)
                            .mesPeriodo(yearMonth.getMonthValue())
                            .anioPeriodo(yearMonth.getYear())
                            .diasTrabajadosEfectivos(numerico.diasTrabajadosEfectivos())
                            .diasFaltaInjustificada(numerico.diasFaltaInjustificada())
                            .horasExtrasDiurnasTotales(numerico.horasExtrasDiurnasTotales())
                            .horasExtrasNocturnasTotales(numerico.horasExtrasNocturnasTotales())
                            .montoSalarioBaseProporcional(numerico.montoSalarioBaseProporcional())
                            .montoGananciaExtras(numerico.montoGananciaExtras())
                            .montoDeduccionesFaltas(numerico.montoDeduccionesFaltas())
                            .montoNetoPagar(numerico.montoNetoPagar())
                            .build(),
                    contrato.getTipoContrato(),
                    contrato.getTipoMoneda(),
                    numerico.diasVacacionesAprobadas(),
                    numerico.llegadasTardias(),
                    numerico.horasTrabajadasTotales(),
                    fechaInicio,
                    fechaFin
            );
        }
    }
}













