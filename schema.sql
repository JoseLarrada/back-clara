-- =========================================================================
-- SYSTEM ARCHITECTURE: CLOUDTIME DATABASES SCHEMA (PostgreSQL 15)
-- SECURITY DESIGN: MULTI-TENANT (SHARED TABLE WITH DISCRIMINATOR COLUMN)
-- AUDITABILITY LAYER: FULL COMPLIANCE WITH ENTERPRISE REQUIREMENTS
-- =========================================================================

-- Habilitar la extensión criptográfica para la generación segura de identificadores UUIDv4
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Limpieza preventiva de esquema para inicialización limpia (Opcional en entornos de despliegue)
DROP TABLE IF EXISTS reportes_prenomina_mensual CASCADE;
DROP TABLE IF EXISTS configuracion_recargos_empresa CASCADE;
DROP TABLE IF EXISTS contratos_empleados CASCADE;
DROP TABLE IF EXISTS anomalias_graves_auditoria CASCADE;
DROP TABLE IF EXISTS solicitudes_vacaciones CASCADE;
DROP TABLE IF EXISTS backup_incidencias_justificaciones CASCADE;
DROP TABLE IF EXISTS registro_asistencia CASCADE;
DROP TABLE IF EXISTS geocercas_remotas CASCADE;
DROP TABLE IF EXISTS calendario_hibrido CASCADE;
DROP TABLE IF EXISTS reglas_negocio_horarios CASCADE;
DROP TABLE IF EXISTS empleados CASCADE;
DROP TABLE IF EXISTS empresas CASCADE;

-- =========================================================================
-- 1. TABLA: empresas (Soporta MÓDULO 0: Capa SaaS Multi-tenant)
-- =========================================================================
-- Da cumplimiento directo a los requisitos funcionales RF01, RF02, RF03 y RF06.
CREATE TABLE empresas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(150) NOT NULL,
    nit_rut VARCHAR(20) NOT NULL,
    rubro VARCHAR(50) NOT NULL, -- Clasificación operativa de la organización (RF03)
    limite_empleados INT NOT NULL DEFAULT 50, -- Control de escalamiento y cuotas del plan (RF02)
    estado_licencia VARCHAR(20) NOT NULL DEFAULT 'ACTIVO', -- Estados permitidos: 'ACTIVO', 'SUSPENDIDO' (RF06)
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_empresas_nit_rut UNIQUE (nit_rut),
    CONSTRAINT chk_empresas_estado CHECK (estado_licencia IN ('ACTIVO', 'SUSPENDIDO')),
    CONSTRAINT chk_empresas_limite CHECK (limite_empleados > 0)
);

-- Índice optimizado para el Dashboard Global de Consumo del SuperAdmin (RF05)
CREATE INDEX idx_empresas_estado ON empresas(estado_licencia);


-- =========================================================================
-- 2. TABLA: empleados (Eje de control de identidad y Gobierno Multi-tenant)
-- =========================================================================
-- Abstrae la lógica del RF04 (Aislamiento), RF08 (Carga de Foto Patrón) y RF09 (Modalidades).
CREATE TABLE empleados (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL, -- Columna Discriminadora Core para la segregación lógica de datos (RF04)
    nombre_completo VARCHAR(250) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'EMPLEADO', -- Roles jerárquicos: 'SUPERADMIN', 'ADMIN_RRHH', 'EMPLEADO'
    modalidad_perfil VARCHAR(20) NOT NULL, -- Perfiles de negocio: 'PRESENCIAL', 'HIBRIDO', 'REMOTO' (RF09)
    foto_patron_url VARCHAR(500), -- Almacenamiento persistente en S3 de la biometría base (RF08, RF24)
    saldo_vacaciones INT NOT NULL DEFAULT 15, -- Bolsa de días disponibles del trabajador (RF21)
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_empleados_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT uk_empleados_email UNIQUE (email),
    CONSTRAINT chk_empleados_rol CHECK (rol IN ('SUPERADMIN', 'ADMIN_RRHH', 'EMPLEADO')),
    CONSTRAINT chk_empleados_modalidad CHECK (modalidad_perfil IN ('PRESENCIAL', 'HIBRIDO', 'REMOTO')),
    CONSTRAINT chk_empleados_vacaciones CHECK (saldo_vacaciones >= 0)
);

-- ÍNDICE CRÍTICO COMPUESTO: Blindaje absoluto. Fuerza al optimizador de queries a filtrar por Tenant ID
CREATE INDEX idx_empleados_tenant ON empleados(empresa_id, id);
CREATE INDEX idx_empleados_email ON empleados(email);


-- =========================================================================
-- 3. TABLA: reglas_negocio_horarios (Soporta MÓDULO 1: Configuración RRHH)
-- =========================================================================
-- Implementa de forma dinámica los parámetros de tolerancia y penalizaciones del RF12.
CREATE TABLE reglas_negocio_horarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL,
    descripcion VARCHAR(100) NOT NULL,
    hora_entrada_oficial TIME NOT NULL,
    hora_salida_oficial TIME NOT NULL,
    minutos_tolerancia_retardo INT NOT NULL DEFAULT 10, -- Margen antes de marcar Retardo (RF12, RF29)
    tiempo_limite_falta_minutos INT NOT NULL DEFAULT 120, -- Tiempo de abandono antes de Falta Automática (RF30)
    CONSTRAINT fk_reglas_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    CONSTRAINT chk_reglas_tolerancia CHECK (minutos_tolerancia_retardo >= 0),
    CONSTRAINT chk_reglas_limite_falta CHECK (tiempo_limite_falta_minutos > 0)
);

CREATE INDEX idx_reglas_empresa ON reglas_negocio_horarios(empresa_id);


-- =========================================================================
-- 4. TABLA: calendario_hibrido (Soporta MÓDULO 1 y MÓDULO 2)
-- =========================================================================
-- Resuelve la asignación de turnos flexibles del RF10 y la restricción del RF27.
CREATE TABLE calendario_hibrido (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empleado_id UUID NOT NULL,
    fecha DATE NOT NULL,
    caracter_dia VARCHAR(20) NOT NULL, -- Dominios permitidos: 'PRESENCIAL' o 'REMOTO' (RF10, RF27)
    CONSTRAINT fk_calendario_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE,
    CONSTRAINT uk_empleado_fecha UNIQUE (empleado_id, fecha),
    CONSTRAINT chk_calendario_caracter CHECK (caracter_dia IN ('PRESENCIAL', 'REMOTO'))
);


-- =========================================================================
-- 5. TABLA: geocercas_remotas (Soporta MÓDULO 1 y MÓDULO 3)
-- =========================================================================
-- Almacena las coordenadas inmutables del RF11 para el análisis geométrico de Geofencing (RF25).
CREATE TABLE geocercas_remotas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empleado_id UUID NOT NULL,
    descripcion VARCHAR(100) NOT NULL DEFAULT 'Casa / Home Office',
    latitud NUMERIC(10, 8) NOT NULL, -- Coordenada GPS de alta precisión (RF11)
    longitud NUMERIC(11, 8) NOT NULL, -- Coordenada GPS de alta precisión (RF11)
    radio_tolerancia_metros INT NOT NULL DEFAULT 50, -- Radio perimetral permitido (RF11, RF25)
    CONSTRAINT fk_geocercas_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE,
    CONSTRAINT chk_geocercas_radio CHECK (radio_tolerancia_metros > 0)
);


-- =========================================================================
-- 6. TABLA: registro_asistencia (Núcleo Transaccional Inmutable del Sistema)
-- =========================================================================
-- Soporta RF16, RF17, RF18, RF23, RF24, RF26, RF28, RF29 y RF30.
CREATE TABLE registro_asistencia (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL, -- Duplicación controlada (Desnormalización) para acelerar reportes agregados sin JOINs (RF04, RF15)
    empleado_id UUID NOT NULL,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    hora_entrada TIMESTAMP WITH TIME ZONE NOT NULL, -- Timestamp con zona horaria del servidor (RF18)
    hora_salida TIMESTAMP WITH TIME ZONE, -- Nullable hasta que se ejecute la marca de fin de turno (RF17)
    hora_almuerzo_inicio TIMESTAMP WITH TIME ZONE, -- Marca de inicio de almuerzo (RF18, RF19)
    hora_almuerzo_fin TIMESTAMP WITH TIME ZONE, -- Marca de fin de almuerzo (RF18, RF19)
    modalidad_aplicada VARCHAR(20) NOT NULL, -- 'PRESENCIAL', 'REMOTO' (RF16)
    estado_entrada VARCHAR(20) NOT NULL DEFAULT 'A_TIEMPO', -- 'A_TIEMPO', 'RETARDO', 'FALTA_JUSTIFICADA', 'FALTA_INJUSTIFICADA' (RF29, RF30)
    es_facial_verificado BOOLEAN NOT NULL DEFAULT FALSE, -- Estado de verificación biométrica de identidad (RF24)
    precision_gps_accuracy NUMERIC(5,2), -- Captura del metadato de hardware del navegador para auditoría de Mock Locations (RF26, RF71)
    token_qr_utilizado VARCHAR(255), -- Hash del código dinámico de 5 segundos para mitigar ataques de replay (RF28)
    tipo_registro VARCHAR(20) NOT NULL DEFAULT 'ENTRADA', -- 'ENTRADA', 'ALMUERZO', 'SALIDA' (RF18)
    instante_servidor_ultima_marcacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Precisión temporal de la última acción (RF18)
    CONSTRAINT fk_asistencia_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_asistencia_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE RESTRICT,
    -- RESTRICCIÓN DETERMINÍSTICA: Un empleado solo puede tener una única marca maestra por día (RF23)
    CONSTRAINT uk_asistencia_empleado_dia UNIQUE (empleado_id, fecha),
    CONSTRAINT chk_asistencia_modalidad CHECK (modalidad_aplicada IN ('PRESENCIAL', 'REMOTO')),
    CONSTRAINT chk_asistencia_estado CHECK (estado_entrada IN ('A_TIEMPO', 'RETARDO', 'FALTA_JUSTIFICADA', 'FALTA_INJUSTIFICADA'))
);

-- Índices masivos para optimizar el Dashboard en Tiempo Real (RF07) y las exportaciones agregadas (RF15)
CREATE INDEX idx_asistencia_fecha_tenant ON registro_asistencia(empresa_id, fecha);
CREATE INDEX idx_asistencia_empleado ON registro_asistencia(empleado_id);


-- =========================================================================
-- 7. TABLA: backup_incidencias_justificaciones (MÓDULO 1 y MÓDULO 2 - Workflow)
-- =========================================================================
-- Almacena las evidencias de auditoría e imágenes de soporte solicitadas en el RF20 y aprobadas en el RF13.
CREATE TABLE backup_incidencias_justificaciones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    registro_asistencia_id UUID NOT NULL,
    motivo_empleado TEXT NOT NULL,
    url_comprobante_s3 VARCHAR(500) NOT NULL, -- Enlace físico inmutable al archivo probatorio (RF20)
    estado_solicitud VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- Máquina de estados: 'PENDIENTE', 'APROBADO', 'RECHAZADO' (RF13)
    comentarios_administrador TEXT,
    procesado_en TIMESTAMP WITH TIME ZONE,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_justificaciones_registro FOREIGN KEY (registro_asistencia_id) REFERENCES registro_asistencia(id) ON DELETE CASCADE,
    CONSTRAINT chk_justificaciones_estado CHECK (estado_solicitud IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
);


-- =========================================================================
-- 8. TABLA: solicitudes_vacaciones (Soporta MÓDULO 1 y MÓDULO 2)
-- =========================================================================
-- Maneja el flujo del RF13, la petición del RF21 y el descuento automático del RF14.
CREATE TABLE solicitudes_vacaciones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empleado_id UUID NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado_solicitud VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'APROBADO', 'RECHAZADO'
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vacaciones_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE,
    CONSTRAINT chk_vacaciones_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_vacaciones_estado CHECK (estado_solicitud IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
);


-- =========================================================================
-- 9. TABLA: anomalias_graves_auditoria (Soporta MÓDULO 3: Motor de Seguridad)
-- =========================================================================
-- Almacena la telemetría de fraude detectada por el hardware (RF26) y el estado de la alerta asíncrona SNS (RF31, RF92).
CREATE TABLE anomalias_graves_auditoria (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empleado_id UUID NOT NULL,
    tipo_anomalia VARCHAR(50) NOT NULL, -- 'MOCK_LOCATION_DETECTADA', 'FACE_MISMATCH', 'FUERA_DE_GEOCERCA'
    detalles_tecnicos TEXT NOT NULL, -- Volcado de metadatos JSON (Ej: accuracy = 0 enviado por atacante) (RF71)
    notificado_via_sns BOOLEAN NOT NULL DEFAULT FALSE, -- Bandera de confirmación de envío al broker de AWS SNS (RF17, RF31)
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_anomalias_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE
);

CREATE INDEX idx_anomalias_empleado ON anomalias_graves_auditoria(empleado_id);


-- =========================================================================
-- 10. TABLA: contratos_empleados (Soporta el Motor Avanzado de Pre-Nómina)
-- =========================================================================
-- Define los parámetros comerciales individuales necesarios para calcular de forma justa el Salario Base Proporcional.
CREATE TABLE contratos_empleados (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empleado_id UUID NOT NULL,
    salario_base_mensual NUMERIC(12, 2) NOT NULL, -- Salario nominal contratado
    tipo_moneda VARCHAR(3) NOT NULL DEFAULT 'COP', -- ISO Currency standard
    tipo_contrato VARCHAR(50) NOT NULL DEFAULT 'TERMINO_INDEFINIDO', 
    fecha_ingreso DATE NOT NULL,
    fecha_retiro DATE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_contratos_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE,
    CONSTRAINT chk_contratos_salario CHECK (salario_base_mensual > 0)
);

CREATE INDEX idx_contratos_empleado ON contratos_empleados(empleado_id) WHERE activo = TRUE;


-- =========================================================================
-- 11. TABLA: configuracion_recargos_empresa (Reglas de Liquidación por Tenant)
-- =========================================================================
-- Parametrización aislada de factores de ley. Permite que el comportamiento del cálculo cambie según la empresa.
CREATE TABLE configuracion_recargos_empresa (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL,
    factor_hora_extra_diurna NUMERIC(4, 2) NOT NULL DEFAULT 1.25, -- Factor multiplicador (Ej: 1.25 representa +25%)
    factor_hora_extra_nocturna NUMERIC(4, 2) NOT NULL DEFAULT 1.75, -- Factor multiplicador
    factor_hora_dominical_festiva NUMERIC(4, 2) NOT NULL DEFAULT 2.00, -- Factor multiplicador
    multa_retardo_por_minuto NUMERIC(10, 2) NOT NULL DEFAULT 0.00, -- Penalización financiera opcional parametrizable
    CONSTRAINT fk_recargos_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

CREATE INDEX idx_recargos_empresa ON configuracion_recargos_empresa(empresa_id);


-- =========================================================================
-- 12. TABLA: reportes_prenomina_mensual (Consolidación de Cierre Contable)
-- =========================================================================
-- Almacena el histórico inmutable calculado a fin de mes. Da cumplimiento directo al RF15 (Exportación de Pre-Nómina).
CREATE TABLE reportes_prenomina_mensual (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL,
    empleado_id UUID NOT NULL,
    mes_periodo INT NOT NULL, -- Rango de control: 1 al 12
    anio_periodo INT NOT NULL, -- Rango de control: >= 2026
    dias_trabajados_efectivos INT NOT NULL DEFAULT 0, -- Calculado contando marcas válidas del mes
    dias_falta_injustificada INT NOT NULL DEFAULT 0, -- Días de inasistencia automática acumulados en el periodo
    horas_extras_diurnas_totales NUMERIC(6, 2) NOT NULL DEFAULT 0.00, -- Cuantificación de tiempo acumulado
    horas_extras_nocturnas_totales NUMERIC(6, 2) NOT NULL DEFAULT 0.00, -- Cuantificación de tiempo acumulado
    
    -- Bloque Analítico Financiero (Procesado por el motor matemático del Backend)
    monto_salario_base_proporcional NUMERIC(12, 2) NOT NULL, -- Salario base devengado ajustado por las inasistencias
    monto_ganancia_extras NUMERIC(12, 2) NOT NULL DEFAULT 0.00, -- Liquidación acumulada multiplicada por los factores del Tenant
    monto_deducciones_faltas NUMERIC(12, 2) NOT NULL DEFAULT 0.00, -- Impacto negativo financiero por faltar de forma injustificada
    monto_neto_pagar NUMERIC(12, 2) NOT NULL, -- Ecuación de Cierre: (Salario Proporcional + Extras) - Deducciones
    
    estado_reporte VARCHAR(20) NOT NULL DEFAULT 'BORRADOR', -- Ciclo de auditoría: 'BORRADOR', 'APROBADO_RRHH', 'PROCESADO_PAGO'
    generado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prenomina_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_prenomina_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE RESTRICT,
    -- RESTRICCIÓN DE UNICIDAD FINANCIERA: Evita duplicar el reporte de un empleado para el mismo periodo contable
    CONSTRAINT uk_prenomina_empleado_periodo UNIQUE (empleado_id, mes_periodo, anio_periodo),
    CONSTRAINT chk_prenomina_mes CHECK (mes_periodo BETWEEN 1 AND 12),
    CONSTRAINT chk_prenomina_anio CHECK (anio_periodo >= 2026)
);

-- Índice compuesto de alta velocidad para las búsquedas, ordenamientos y descargas de reportes en React (RF15)
CREATE INDEX idx_prenomina_busqueda ON reportes_prenomina_mensual(empresa_id, anio_periodo, mes_periodo);