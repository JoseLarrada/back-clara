-- =========================================================================
-- SQL SCRIPT: GENERADOR DE DATOS DE PRUEBA REALES (COLOMBIA)
-- PROYECTO: CLOUDTIME BACKEND
-- =========================================================================

-- Deshabilitar triggers temporalmente para carga masiva limpia
SET session_replication_role = 'replica';

-- 1. LIMPIEZA DE DATOS TRANSACCIONALES PREVIOS (Excluyendo al empleado Carles Perez y su información)
-- NOTA: No borramos de la tabla 'empleados' ni 'empresas', solo limpiamos la data operativa
-- para los empleados que vamos a poblar.
DELETE FROM logs_auditoria_sistema WHERE usuario_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM registro_marcas WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM historial_ubicaciones WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM ultima_ubicacion WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM backup_incidencias_justificaciones WHERE registro_asistencia_id IN (SELECT id FROM registro_asistencia WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317'));
DELETE FROM anomalias_graves_auditoria WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM reportes_prenomina_mensual WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM movimientos_vacaciones WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM solicitudes_vacaciones WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM registro_asistencia WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM geocercas_remotas WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');
DELETE FROM contratos_empleados WHERE empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');

-- Restablecer saldo de vacaciones por defecto en la tabla empleados para los objetivos
UPDATE empleados SET saldo_vacaciones = 15 WHERE id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');

-- 2. INSERCIÓN DE CONFIGURACIONES GENERALES DE EMPRESA (Si no existen)
-- Evaluamos las empresas de los empleados para insertar reglas y factores de recargo
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN 
        SELECT DISTINCT empresa_id FROM empleados 
        WHERE id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317')
    LOOP
        -- Reglas de negocio horarios si no existen
        IF NOT EXISTS (SELECT 1 FROM reglas_negocio_horarios WHERE empresa_id = rec.empresa_id) THEN
            INSERT INTO reglas_negocio_horarios (id, empresa_id, descripcion, hora_entrada_oficial, hora_salida_oficial, minutos_tolerancia_retardo, tiempo_limite_falta_minutos)
            VALUES (uuid_generate_v4(), rec.empresa_id, 'Horario de Oficina Estándar', '08:00:00', '17:00:00', 15, 120);
        END IF;

        -- Configuración de recargos si no existe
        IF NOT EXISTS (SELECT 1 FROM configuracion_recargos_empresa WHERE empresa_id = rec.empresa_id) THEN
            INSERT INTO configuracion_recargos_empresa (id, empresa_id, factor_hora_extra_diurna, factor_hora_extra_nocturna, factor_hora_dominical_festiva, multa_retardo_por_minuto)
            VALUES (uuid_generate_v4(), rec.empresa_id, 1.25, 1.75, 2.00, 500.00);
        END IF;
    END LOOP;
END $$;


-- 3. INSERCIÓN DE CONTRATOS DE TRABAJO REALISTAS (COP)
INSERT INTO contratos_empleados (id, empresa_id, empleado_id, salario_base_mensual, tipo_moneda, tipo_contrato, fecha_ingreso, fecha_retiro, activo)
SELECT uuid_generate_v4(), e.empresa_id, e.id, 
       CASE 
         WHEN e.rol = 'SUPERADMIN' THEN 8500000.00
         WHEN e.rol = 'ADMIN_RRHH' THEN 4800000.00
         ELSE 2500000.00 + (random() * 1500000.00)::numeric(12,2)
       END,
       'COP', 'TERMINO_INDEFINIDO', '2025-01-15', NULL, TRUE
FROM empleados e
WHERE e.id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');


-- 4. INSERCIÓN DE GEOCERCAS EN COLOMBIA POR CIUDAD ASIGNADA
-- Bogotá: Lat 4.60971, Lng -74.08175
-- Medellín: Lat 6.25184, Lng -75.56359
-- Cali: Lat 3.43722, Lng -76.52250
-- Barranquilla: Lat 10.96388, Lng -74.79638
INSERT INTO geocercas_remotas (id, empresa_id, empleado_id, descripcion, latitud, longitud, radio_tolerancia_metros)
VALUES
  -- Jose Larrada (Bogotá)
  (uuid_generate_v4(), (SELECT empresa_id FROM empleados WHERE id = '5e14411f-ead5-44ca-be6f-aab2ae0c2bcc'), '5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', 'Casa - Bogotá Salitre', 4.65340000, -74.10820000, 100),
  (uuid_generate_v4(), (SELECT empresa_id FROM empleados WHERE id = '5e14411f-ead5-44ca-be6f-aab2ae0c2bcc'), '5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', 'Oficina Norte Bogotá', 4.71100000, -74.04000000, 150),
  -- Pedro Sanchez (Bogotá)
  (uuid_generate_v4(), (SELECT empresa_id FROM empleados WHERE id = '63a2abb8-b4e9-4579-a05b-8562155c92d7'), '63a2abb8-b4e9-4579-a05b-8562155c92d7', 'Casa - Bogotá Chapinero', 4.64860000, -74.06120000, 80),
  -- Maria Loren Romani (Medellín)
  (uuid_generate_v4(), (SELECT empresa_id FROM empleados WHERE id = '7a9b662a-c618-48b8-9115-df495d1842af'), '7a9b662a-c618-48b8-9115-df495d1842af', 'Apartamento - Medellín Poblado', 6.20810000, -75.56750000, 100),
  -- Juliana Torres (Cali)
  (uuid_generate_v4(), (SELECT empresa_id FROM empleados WHERE id = '4fdd4dba-2949-4563-83e3-fc3d98209023'), '4fdd4dba-2949-4563-83e3-fc3d98209023', 'Casa - Cali Ciudad Jardín', 3.37120000, -76.53100000, 120),
  -- Anibal Fuentes (Barranquilla)
  (uuid_generate_v4(), (SELECT empresa_id FROM empleados WHERE id = '9e921f56-0675-4ae5-9ceb-5b344b2672e8'), '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'Casa - B/quilla Riomar', 11.01250000, -74.81500000, 100),
  -- Jose Armando Larrada (Bogotá)
  (uuid_generate_v4(), (SELECT empresa_id FROM empleados WHERE id = 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52'), 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'Apartamento - Bogotá Cedritos', 4.72500000, -74.03200000, 100),
  -- Juancho Polo (Medellín)
  (uuid_generate_v4(), (SELECT empresa_id FROM empleados WHERE id = 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317'), 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317', 'Casa - Medellín Laureles', 6.24430000, -75.58920000, 100);


-- 5. PROCEDIMIENTO PL/pgSQL PARA POBLAR REGISTROS MASIVOS (ASISTENCIA, MARCAS, HISTORIAL, ANOMALÍAS)
DO $$
DECLARE
    -- Lista de empleados objetivos
    emp_ids UUID[] := ARRAY[
        '5e14411f-ead5-44ca-be6f-aab2ae0c2bcc'::UUID, -- Jose Larrada
        '63a2abb8-b4e9-4579-a05b-8562155c92d7'::UUID, -- Pedro Sanchez
        '7a9b662a-c618-48b8-9115-df495d1842af'::UUID, -- Maria Loren Romani
        '4fdd4dba-2949-4563-83e3-fc3d98209023'::UUID, -- Juliana Torres
        '9e921f56-0675-4ae5-9ceb-5b344b2672e8'::UUID, -- Anibal Fuentes
        'dfd4425f-cacc-4545-b9f2-9cae9ed61c52'::UUID, -- Jose Armando Larrada
        'b6174a08-0a2a-4a3e-9c58-4acd2b69f317'::UUID  -- Juancho Polo
    ];
    
    emp_id UUID;
    temp_empresa_id UUID;
    temp_modalidad VARCHAR(20);
    fecha_actual DATE;
    dia_semana INT;
    
    -- Parámetros de Simulación de Ubicación (Centro de Ciudad por Empleado)
    c_lat NUMERIC(10,8);
    c_lng NUMERIC(11,8);
    var_lat NUMERIC(10,8);
    var_lng NUMERIC(11,8);
    
    -- Variables temporales de marcas de tiempo
    h_entrada TIMESTAMP WITH TIME ZONE;
    h_salida TIMESTAMP WITH TIME ZONE;
    h_almuerzo_ini TIMESTAMP WITH TIME ZONE;
    h_almuerzo_fin TIMESTAMP WITH TIME ZONE;
    
    -- IDs autogenerados
    asistencia_id UUID;
    anomalia_id UUID;
    
    -- Parámetros del generador aleatorio
    rnd NUMERIC;
    estado_ent VARCHAR(20);
    es_mock BOOLEAN;
    score_fac NUMERIC(5,2);
    gps_prec NUMERIC(10,2);
    notif_sns BOOLEAN;
BEGIN
    -- Ciclo para iterar por cada uno de los empleados
    FOREACH emp_id IN ARRAY emp_ids LOOP
        
        -- Obtener datos básicos del empleado
        SELECT empresa_id, modalidad_perfil INTO temp_empresa_id, temp_modalidad FROM empleados WHERE id = emp_id;
        
        -- Asignar el centro geográfico aproximado de simulación según la ciudad
        IF emp_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52') THEN
            -- Bogotá
            c_lat := 4.65000000; c_lng := -74.08000000;
        ELSIF emp_id IN ('7a9b662a-c618-48b8-9115-df495d1842af', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317') THEN
            -- Medellín
            c_lat := 6.24000000; c_lng := -75.57000000;
        ELSIF emp_id = '4fdd4dba-2949-4563-83e3-fc3d98209023' THEN
            -- Cali
            c_lat := 3.44000000; c_lng := -76.53000000;
        ELSE
            -- Barranquilla
            c_lat := 11.00000000; c_lng := -74.80000000;
        END IF;

        -- Generar historial de asistencia diario para el periodo: 2026-05-01 al 2026-06-04 (35 días)
        FOR i IN 0..34 LOOP
            fecha_actual := '2026-05-01'::DATE + i;
            dia_semana := EXTRACT(ISODOW FROM fecha_actual);
            
            -- Omitir fines de semana de forma general (Sábado = 6, Domingo = 7)
            -- Excepto un 15% de probabilidad de tener trabajo en fin de semana para acumular horas dominicales.
            IF dia_semana IN (6, 7) AND random() > 0.15 THEN
                CONTINUE;
            END IF;
            
            -- 5% de probabilidad de faltar de forma injustificada (no se genera registro para ese día)
            -- Omitimos esto para el superadmin
            IF random() < 0.05 AND emp_id != '5e14411f-ead5-44ca-be6f-aab2ae0c2bcc' THEN
                CONTINUE;
            END IF;

            -- Generar variaciones de coordenadas GPS muy realistas (cerca al punto central)
            var_lat := c_lat + ((random() - 0.5) * 0.015)::numeric(10,8);
            var_lng := c_lng + ((random() - 0.5) * 0.015)::numeric(11,8);
            gps_prec := (5.0 + random() * 15.0)::numeric(10,2);
            
            -- Definir si la entrada es a tiempo o retardo
            rnd := random();
            IF rnd < 0.82 THEN
                -- A tiempo (Entrada entre 07:45 AM y 08:00 AM)
                h_entrada := (fecha_actual::text || ' ' || (07 + floor(random()*1))::text || ':' || (45 + floor(random()*15))::text || ':' || floor(random()*60)::text || ' -05:00')::timestamp with time zone;
                estado_ent := 'A_TIEMPO';
            ELSIF rnd < 0.95 THEN
                -- Retardo (Entrada entre 08:01 AM y 08:35 AM)
                h_entrada := (fecha_actual::text || ' 08:' || (01 + floor(random()*34))::text || ':' || floor(random()*60)::text || ' -05:00')::timestamp with time zone;
                estado_ent := 'RETARDO';
            ELSE
                -- Falta Justificada (Por ejemplo se generará un comprobante de justificación después)
                h_entrada := (fecha_actual::text || ' 10:15:00 -05:00')::timestamp with time zone;
                estado_ent := 'FALTA_JUSTIFICADA';
            END IF;

            -- Definir Almuerzo (Inicio entre 12:00 y 12:45, Duración aproximada de 1 hora)
            h_almuerzo_ini := (fecha_actual::text || ' 12:' || (floor(random()*45))::text || ':00 -05:00')::timestamp with time zone;
            h_almuerzo_fin := h_almuerzo_ini + '1 hour'::interval + (floor(random()*10)::text || ' minutes')::interval;
            
            -- Definir Salida (Entre 05:00 PM y 05:45 PM)
            h_salida := (fecha_actual::text || ' 17:' || (floor(random()*45))::text || ':00 -05:00')::timestamp with time zone;
            
            -- Simular score de coincidencia facial biométrica (típicamente > 85%, excepcionalmente baja)
            score_fac := (80.00 + random() * 20.00)::numeric(5,2);
            IF random() < 0.02 THEN
                score_fac := (40.00 + random() * 20.00)::numeric(5,2); -- Mismatch de prueba
            END IF;

            -- Simular Mock Location
            es_mock := FALSE;
            IF random() < 0.02 AND emp_id != '5e14411f-ead5-44ca-be6f-aab2ae0c2bcc' THEN
                es_mock := TRUE;
            END IF;

            -- Crear ID del Registro de Asistencia
            asistencia_id := uuid_generate_v4();

            -- Insertar en registro_asistencia
            INSERT INTO registro_asistencia (
                id, empresa_id, empleado_id, fecha, hora_entrada, hora_salida, 
                hora_almuerzo_inicio, hora_almuerzo_fin, modalidad_aplicada, 
                estado_entrada, es_facial_verificado, precision_gps_accuracy, 
                token_qr_utilizado, latitud, longitud, es_mock_location, 
                foto_captura_url, score_facial_coincidencia, tipo_registro, instante_servidor_ultima_marcacion
            ) VALUES (
                asistencia_id, temp_empresa_id, emp_id, fecha_actual, h_entrada, h_salida,
                h_almuerzo_ini, h_almuerzo_fin, 
                CASE 
                    WHEN temp_modalidad = 'HIBRIDO' THEN (CASE WHEN random() > 0.5 THEN 'PRESENCIAL' ELSE 'REMOTO' END)
                    ELSE temp_modalidad
                END,
                estado_ent, 
                CASE WHEN score_fac >= 80 THEN TRUE ELSE FALSE END,
                gps_prec,
                md5(random()::text), var_lat, var_lng, es_mock,
                'https://cloudtime-media-storage-dev.s3.amazonaws.com/capturas/' || asistencia_id::text || '.jpg',
                score_fac, 'SALIDA', h_salida
            );

            -- Insertar marcas en registro_marcas (Ledger) correspondientes
            INSERT INTO registro_marcas (id, empresa_id, empleado_id, tipo_marca, fecha_hora, modalidad, foto_captura_url, score_facial_coincidencia, latitud, longitud, precision_gps_accuracy, es_mock_location)
            VALUES 
                (uuid_generate_v4(), temp_empresa_id, emp_id, 'ENTRADA', h_entrada, temp_modalidad, 'https://cloudtime-media-storage-dev.s3.amazonaws.com/capturas/' || asistencia_id::text || '_in.jpg', score_fac, var_lat, var_lng, gps_prec, es_mock),
                (uuid_generate_v4(), temp_empresa_id, emp_id, 'INICIO_ALMUERZO', h_almuerzo_ini, temp_modalidad, NULL, NULL, var_lat, var_lng, gps_prec, es_mock),
                (uuid_generate_v4(), temp_empresa_id, emp_id, 'FIN_ALMUERZO', h_almuerzo_fin, temp_modalidad, NULL, NULL, var_lat, var_lng, gps_prec, es_mock),
                (uuid_generate_v4(), temp_empresa_id, emp_id, 'SALIDA', h_salida, temp_modalidad, 'https://cloudtime-media-storage-dev.s3.amazonaws.com/capturas/' || asistencia_id::text || '_out.jpg', score_fac, var_lat, var_lng, gps_prec, es_mock);

            -- Generar historial de ubicaciones durante el día laboral (simulando trayecto/monitoreo en tiempo real)
            FOR j IN 1..4 LOOP
                INSERT INTO historial_ubicaciones (id, empresa_id, empleado_id, latitud, longitud, precision_gps, velocidad, direccion, registrado_en)
                VALUES (
                    uuid_generate_v4(), temp_empresa_id, emp_id,
                    (var_lat + ((j - 2.5) * 0.0012) + (random() * 0.0003))::numeric(10,8),
                    (var_lng + ((j - 2.5) * 0.0012) + (random() * 0.0003))::numeric(11,8),
                    gps_prec,
                    (5.0 + random() * 45.0)::numeric(10,2), -- Velocidad en km/h
                    (random() * 360.0)::numeric(10,2),     -- Dirección en grados
                    h_entrada + (j * 2) * INTERVAL '1 hour'
                );
            END LOOP;

            -- Generar anomalías graves de auditoría si se cumplen condiciones ficticias
            -- Caso 1: Score facial menor a 80
            IF score_fac < 80 THEN
                anomalia_id := uuid_generate_v4();
                INSERT INTO anomalias_graves_auditoria (id, empresa_id, empleado_id, tipo_anomalia, detalles_tecnicos, notificado_via_sns)
                VALUES (
                    anomalia_id, temp_empresa_id, emp_id, 'FACE_MISMATCH',
                    '{"confidence_score": ' || score_fac || ', "threshold": 80.0, "camera": "front-facing", "device": "Android App v2.1"}',
                    TRUE
                );
            END IF;

            -- Caso 2: Es Mock Location
            IF es_mock THEN
                anomalia_id := uuid_generate_v4();
                INSERT INTO anomalias_graves_auditoria (id, empresa_id, empleado_id, tipo_anomalia, detalles_tecnicos, notificado_via_sns)
                VALUES (
                    anomalia_id, temp_empresa_id, emp_id, 'MOCK_LOCATION_DETECTADA',
                    '{"provider": "gps", "accuracy": ' || gps_prec || ', "mock_apps_installed": ["FakeGPS Pro"], "system_time_mismatch_ms": 1400}',
                    TRUE
                );
            END IF;

            -- Caso 3: Fuera de geocerca (Simulado en un 3% de los registros para empleados presenciales/híbridos)
            IF temp_modalidad IN ('PRESENCIAL', 'HIBRIDO') AND random() < 0.03 AND emp_id != '5e14411f-ead5-44ca-be6f-aab2ae0c2bcc' THEN
                anomalia_id := uuid_generate_v4();
                INSERT INTO anomalias_graves_auditoria (id, empresa_id, empleado_id, tipo_anomalia, detalles_tecnicos, notificado_via_sns)
                VALUES (
                    anomalia_id, temp_empresa_id, emp_id, 'FUERA_DE_GEOCERCA',
                    '{"distancia_metros_exceso": ' || floor(180 + random() * 450) || ', "geocerca_radio": 100, "precision_gps": ' || gps_prec || '}',
                    TRUE
                );
            END IF;

            -- Crear Justificación si es Falta Justificada o Retardo (50% de probabilidad para retardo)
            IF estado_ent = 'FALTA_JUSTIFICADA' OR (estado_ent = 'RETARDO' AND random() > 0.5) THEN
                INSERT INTO backup_incidencias_justificaciones (id, registro_asistencia_id, motivo_empleado, url_comprobante_s3, estado_solicitud, comentarios_administrador, procesado_en)
                VALUES (
                    uuid_generate_v4(), asistencia_id,
                    CASE 
                        WHEN estado_ent = 'FALTA_JUSTIFICADA' THEN 'Cita médica prioritaria de control general en EPS Sura.'
                        ELSE 'Retraso masivo en el sistema de transporte Transmilenio / Metro debido a lluvias torrenciales.'
                    END,
                    'https://cloudtime-media-storage-dev.s3.amazonaws.com/justificaciones/' || asistencia_id::text || '_evidence.pdf',
                    CASE WHEN random() > 0.4 THEN 'APROBADO' ELSE 'PENDIENTE' END,
                    CASE WHEN random() > 0.4 THEN 'Se aprueba de manera excepcional tras validar soporte clínico.' ELSE NULL END,
                    CASE WHEN random() > 0.4 THEN h_salida + '1 day'::interval ELSE NULL END
                );
            END IF;

        END LOOP;
        
        -- Insertar última ubicación conocida del empleado para el mapa en tiempo real
        INSERT INTO ultima_ubicacion (empleado_id, empresa_id, latitud, longitud, precision_gps, velocidad, direccion, estado_conexion, ultima_actualizacion)
        VALUES (
            emp_id, temp_empresa_id,
            (c_lat + ((random() - 0.5) * 0.005))::numeric(10,8),
            (c_lng + ((random() - 0.5) * 0.005))::numeric(11,8),
            (3.0 + random() * 10.0)::numeric(10,2),
            (0.0 + random() * 5.0)::numeric(10,2),
            (random() * 360.0)::numeric(10,2),
            CASE 
                WHEN random() > 0.3 THEN 'ACTIVO'
                WHEN random() > 0.5 THEN 'INACTIVO'
                ELSE 'DESCONECTADO'
            END,
            CURRENT_TIMESTAMP - (random() * 60)::integer * INTERVAL '1 minute'
        );
        
    END LOOP;
END $$;


-- 6. INSERCIÓN DE HISTORIAL DE VACACIONES PARA CADA EMPLEADO
DO $$
DECLARE
    emp_ids UUID[] := ARRAY[
        '5e14411f-ead5-44ca-be6f-aab2ae0c2bcc'::UUID,
        '63a2abb8-b4e9-4579-a05b-8562155c92d7'::UUID,
        '7a9b662a-c618-48b8-9115-df495d1842af'::UUID,
        '4fdd4dba-2949-4563-83e3-fc3d98209023'::UUID,
        '9e921f56-0675-4ae5-9ceb-5b344b2672e8'::UUID,
        'dfd4425f-cacc-4545-b9f2-9cae9ed61c52'::UUID,
        'b6174a08-0a2a-4a3e-9c58-4acd2b69f317'::UUID
    ];
    emp_id UUID;
    temp_empresa_id UUID;
    solicitud_id UUID;
BEGIN
    FOREACH emp_id IN ARRAY emp_ids LOOP
        SELECT empresa_id INTO temp_empresa_id FROM empleados WHERE id = emp_id;
        
        -- Insertar movimientos de devengado por ley inicial (Bolsa inicial de 15 días)
        INSERT INTO movimientos_vacaciones (id, empresa_id, empleado_id, tipo_movimiento, cantidad_dias, solicitud_id, motivo_ajuste, creado_por, creado_en)
        VALUES (uuid_generate_v4(), temp_empresa_id, emp_id, 'DEVENGADO_LEY', 15, NULL, 'Carga inicial de saldo legal devengado por año de servicios.', NULL, '2026-01-01 00:00:00 -05:00');
        
        -- Generar solicitud tomada aprobada (Ej: 5 días en marzo 2026)
        solicitud_id := uuid_generate_v4();
        INSERT INTO solicitudes_vacaciones (id, empresa_id, empleado_id, fecha_inicio, fecha_fin, estado_solicitud, creado_en)
        VALUES (solicitud_id, temp_empresa_id, emp_id, '2026-03-10', '2026-03-14', 'APROBADO', '2026-03-01 09:00:00 -05:00');
        
        INSERT INTO movimientos_vacaciones (id, empresa_id, empleado_id, tipo_movimiento, cantidad_dias, solicitud_id, motivo_ajuste, creado_por, creado_en)
        VALUES (uuid_generate_v4(), temp_empresa_id, emp_id, 'TOMADO_APROBADO', -5, solicitud_id, NULL, '63a2abb8-b4e9-4579-a05b-8562155c92d7', '2026-03-05 14:00:00 -05:00');
        
        -- Generar solicitud pendiente para junio 2026 (Para pruebas en Dashboard)
        IF random() > 0.4 THEN
            INSERT INTO solicitudes_vacaciones (id, empresa_id, empleado_id, fecha_inicio, fecha_fin, estado_solicitud, creado_en)
            VALUES (uuid_generate_v4(), temp_empresa_id, emp_id, '2026-06-20', '2026-06-25', 'PENDIENTE', CURRENT_TIMESTAMP);
        END IF;
    END LOOP;
END $$;


-- 7. INSERCIÓN DE CONSOLIDADO DE PRENÓMINA MENSUAL (Periodo Mayo 2026)
INSERT INTO reportes_prenomina_mensual (
    id, empresa_id, empleado_id, mes_periodo, anio_periodo, 
    dias_trabajados_efectivos, dias_falta_injustificada, 
    horas_extras_diurnas_totales, horas_extras_nocturnas_totales,
    monto_salario_base_proporcional, monto_ganancia_extras, 
    monto_deducciones_faltas, monto_neto_pagar, estado_reporte, requiere_recalculo, generado_el
)
SELECT 
    uuid_generate_v4(),
    c.empresa_id,
    c.empleado_id,
    5, -- Mayo
    2026,
    22 - (random() * 2)::integer, -- Días laborables aproximados en el mes
    (random() * 1)::integer,      -- Faltas aleatorias
    (random() * 12.0)::numeric(6,2), -- Horas extra diurnas
    (random() * 6.0)::numeric(6,2),  -- Horas extra nocturnas
    c.salario_base_mensual,
    0.00, -- Se calculará
    0.00, -- Se calculará
    c.salario_base_mensual, -- Neto tentativo
    'BORRADOR',
    TRUE, -- Marcado para cálculo matemático por backend
    CURRENT_TIMESTAMP
FROM contratos_empleados c
WHERE c.empleado_id IN ('5e14411f-ead5-44ca-be6f-aab2ae0c2bcc', '63a2abb8-b4e9-4579-a05b-8562155c92d7', '7a9b662a-c618-48b8-9115-df495d1842af', '4fdd4dba-2949-4563-83e3-fc3d98209023', '9e921f56-0675-4ae5-9ceb-5b344b2672e8', 'dfd4425f-cacc-4545-b9f2-9cae9ed61c52', 'b6174a08-0a2a-4a3e-9c58-4acd2b69f317');


-- 8. RE-CÁLCULO FINANCIERO POSTGRES PARA PRENÓMINA DE PRUEBA
DO $$
DECLARE
    rec RECORD;
    valor_hora NUMERIC(15,2);
    ganancia_ex NUMERIC(15,2);
    deduc_faltas NUMERIC(15,2);
BEGIN
    FOR rec IN SELECT * FROM reportes_prenomina_mensual WHERE mes_periodo = 5 AND anio_periodo = 2026 LOOP
        -- Obtener salario mensual
        SELECT salario_base_mensual INTO valor_hora FROM contratos_empleados WHERE empleado_id = rec.empleado_id;
        
        -- Calcular valor hora ordinario (240 horas mensuales de ley en Colombia)
        valor_hora := valor_hora / 240.0;
        
        -- Ganancia por horas extras:
        -- Extras Diurnas (1.25x) + Extras Nocturnas (1.75x)
        ganancia_ex := (rec.horas_extras_diurnas_totales * valor_hora * 1.25) + 
                       (rec.horas_extras_nocturnas_totales * valor_hora * 1.75);
                       
        -- Deducciones por faltas injustificadas (1 día de descuento por cada falta = 8 horas laborales)
        deduc_faltas := rec.dias_falta_injustificada * valor_hora * 8.0;
        
        UPDATE reportes_prenomina_mensual
        SET 
            monto_salario_base_proporcional = (SELECT salario_base_mensual FROM contratos_empleados WHERE empleado_id = rec.empleado_id),
            monto_ganancia_extras = ganancia_ex::numeric(12,2),
            monto_deducciones_faltas = deduc_faltas::numeric(12,2),
            monto_neto_pagar = ((SELECT salario_base_mensual FROM contratos_empleados WHERE empleado_id = rec.empleado_id) + ganancia_ex - deduc_faltas)::numeric(12,2),
            requiere_recalculo = FALSE
        WHERE id = rec.id;
    END LOOP;
END $$;


-- Habilitar triggers de vuelta a la normalidad
SET session_replication_role = 'origin';

-- =========================================================================
-- FIN DEL SCRIPT: DATOS CARGADOS CORRECTAMENTE (CIENTOS DE REGISTROS)
-- =========================================================================
