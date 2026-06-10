-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.empresas (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  nombre character varying NOT NULL,
  nit_rut character varying NOT NULL UNIQUE,
  rubro character varying NOT NULL,
  limite_empleados integer NOT NULL DEFAULT 50 CHECK (limite_empleados > 0),
  estado_licencia character varying NOT NULL DEFAULT 'ACTIVO'::character varying CHECK (estado_licencia::text = ANY (ARRAY['ACTIVO'::character varying, 'SUSPENDIDO'::character varying]::text[])),
  creado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  actualizado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT empresas_pkey PRIMARY KEY (id)
);
CREATE TABLE public.empleados (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empresa_id uuid NOT NULL,
  nombre_completo character varying NOT NULL,
  email character varying NOT NULL UNIQUE,
  password_hash character varying NOT NULL,
  rol character varying NOT NULL DEFAULT 'EMPLEADO'::character varying CHECK (rol::text = ANY (ARRAY['SUPERADMIN'::character varying, 'ADMIN_RRHH'::character varying, 'EMPLEADO'::character varying]::text[])),
  modalidad_perfil character varying NOT NULL CHECK (modalidad_perfil::text = ANY (ARRAY['PRESENCIAL'::character varying, 'HIBRIDO'::character varying, 'REMOTO'::character varying]::text[])),
  foto_patron_url character varying,
  saldo_vacaciones integer NOT NULL DEFAULT 15 CHECK (saldo_vacaciones >= 0),
  activo boolean NOT NULL DEFAULT true,
  creado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT empleados_pkey PRIMARY KEY (id),
  CONSTRAINT fk_empleados_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.reglas_negocio_horarios (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empresa_id uuid NOT NULL,
  descripcion character varying NOT NULL,
  hora_entrada_oficial time without time zone NOT NULL,
  hora_salida_oficial time without time zone NOT NULL,
  minutos_tolerancia_retardo integer NOT NULL DEFAULT 10 CHECK (minutos_tolerancia_retardo >= 0),
  tiempo_limite_falta_minutos integer NOT NULL DEFAULT 120 CHECK (tiempo_limite_falta_minutos > 0),
  CONSTRAINT reglas_negocio_horarios_pkey PRIMARY KEY (id),
  CONSTRAINT fk_reglas_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.calendario_hibrido (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empleado_id uuid NOT NULL,
  fecha date NOT NULL,
  caracter_dia character varying NOT NULL CHECK (caracter_dia::text = ANY (ARRAY['PRESENCIAL'::character varying, 'REMOTO'::character varying]::text[])),
  CONSTRAINT calendario_hibrido_pkey PRIMARY KEY (id),
  CONSTRAINT fk_calendario_empleado FOREIGN KEY (empleado_id) REFERENCES public.empleados(id)
);
CREATE TABLE public.geocercas_remotas (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empleado_id uuid NOT NULL,
  descripcion character varying NOT NULL DEFAULT 'Casa / Home Office'::character varying,
  latitud numeric NOT NULL,
  longitud numeric NOT NULL,
  radio_tolerancia_metros integer NOT NULL DEFAULT 50 CHECK (radio_tolerancia_metros > 0),
  empresa_id uuid,
  CONSTRAINT geocercas_remotas_pkey PRIMARY KEY (id),
  CONSTRAINT fk_geocercas_empleado FOREIGN KEY (empleado_id) REFERENCES public.empleados(id),
  CONSTRAINT geocercas_remotas_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.registro_asistencia (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empresa_id uuid NOT NULL,
  empleado_id uuid NOT NULL,
  fecha date NOT NULL DEFAULT CURRENT_DATE,
  hora_entrada timestamp with time zone NOT NULL,
  hora_salida timestamp with time zone,
  hora_almuerzo_inicio timestamp with time zone,
  hora_almuerzo_fin timestamp with time zone,
  modalidad_aplicada character varying NOT NULL CHECK (modalidad_aplicada::text = ANY (ARRAY['PRESENCIAL'::character varying, 'REMOTO'::character varying]::text[])),
  estado_entrada character varying NOT NULL DEFAULT 'A_TIEMPO'::character varying CHECK (estado_entrada::text = ANY (ARRAY['A_TIEMPO'::character varying, 'RETARDO'::character varying, 'FALTA_JUSTIFICADA'::character varying, 'FALTA_INJUSTIFICADA'::character varying]::text[])),
  es_facial_verificado boolean NOT NULL DEFAULT false,
  precision_gps_accuracy numeric,
  token_qr_utilizado character varying,
  tipo_registro character varying NOT NULL DEFAULT 'ENTRADA'::character varying,
  instante_servidor_ultima_marcacion timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
  latitud numeric,
  longitud numeric,
  es_mock_location boolean DEFAULT false,
  foto_captura_url character varying,
  score_facial_coincidencia numeric,
  CONSTRAINT registro_asistencia_pkey PRIMARY KEY (id),
  CONSTRAINT fk_asistencia_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id),
  CONSTRAINT fk_asistencia_empleado FOREIGN KEY (empleado_id) REFERENCES public.empleados(id)
);
CREATE TABLE public.backup_incidencias_justificaciones (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  registro_asistencia_id uuid NOT NULL,
  motivo_empleado text NOT NULL,
  url_comprobante_s3 character varying NOT NULL,
  estado_solicitud character varying NOT NULL DEFAULT 'PENDIENTE'::character varying CHECK (estado_solicitud::text = ANY (ARRAY['PENDIENTE'::character varying, 'APROBADO'::character varying, 'RECHAZADO'::character varying]::text[])),
  comentarios_administrador text,
  procesado_en timestamp with time zone,
  creado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT backup_incidencias_justificaciones_pkey PRIMARY KEY (id),
  CONSTRAINT fk_justificaciones_registro FOREIGN KEY (registro_asistencia_id) REFERENCES public.registro_asistencia(id)
);
CREATE TABLE public.solicitudes_vacaciones (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empleado_id uuid NOT NULL,
  fecha_inicio date NOT NULL,
  fecha_fin date NOT NULL,
  estado_solicitud character varying NOT NULL DEFAULT 'PENDIENTE'::character varying CHECK (estado_solicitud::text = ANY (ARRAY['PENDIENTE'::character varying, 'APROBADO'::character varying, 'RECHAZADO'::character varying]::text[])),
  creado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  empresa_id uuid,
  CONSTRAINT solicitudes_vacaciones_pkey PRIMARY KEY (id),
  CONSTRAINT fk_vacaciones_empleado FOREIGN KEY (empleado_id) REFERENCES public.empleados(id),
  CONSTRAINT solicitudes_vacaciones_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.anomalias_graves_auditoria (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empleado_id uuid NOT NULL,
  tipo_anomalia character varying NOT NULL,
  detalles_tecnicos text NOT NULL,
  notificado_via_sns boolean NOT NULL DEFAULT false,
  creado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  empresa_id uuid,
  CONSTRAINT anomalias_graves_auditoria_pkey PRIMARY KEY (id),
  CONSTRAINT fk_anomalias_empleado FOREIGN KEY (empleado_id) REFERENCES public.empleados(id),
  CONSTRAINT anomalias_graves_auditoria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.contratos_empleados (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empleado_id uuid NOT NULL,
  salario_base_mensual numeric NOT NULL CHECK (salario_base_mensual > 0::numeric),
  tipo_moneda character varying NOT NULL DEFAULT 'COP'::character varying,
  tipo_contrato character varying NOT NULL DEFAULT 'TERMINO_INDEFINIDO'::character varying,
  fecha_ingreso date NOT NULL,
  fecha_retiro date,
  activo boolean NOT NULL DEFAULT true,
  empresa_id uuid,
  CONSTRAINT contratos_empleados_pkey PRIMARY KEY (id),
  CONSTRAINT fk_contratos_empleado FOREIGN KEY (empleado_id) REFERENCES public.empleados(id),
  CONSTRAINT contratos_empleados_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.configuracion_recargos_empresa (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empresa_id uuid NOT NULL,
  factor_hora_extra_diurna numeric NOT NULL DEFAULT 1.25,
  factor_hora_extra_nocturna numeric NOT NULL DEFAULT 1.75,
  factor_hora_dominical_festiva numeric NOT NULL DEFAULT 2.00,
  multa_retardo_por_minuto numeric NOT NULL DEFAULT 0.00,
  CONSTRAINT configuracion_recargos_empresa_pkey PRIMARY KEY (id),
  CONSTRAINT fk_recargos_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.reportes_prenomina_mensual (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empresa_id uuid NOT NULL,
  empleado_id uuid NOT NULL,
  mes_periodo integer NOT NULL CHECK (mes_periodo >= 1 AND mes_periodo <= 12),
  anio_periodo integer NOT NULL CHECK (anio_periodo >= 2026),
  dias_trabajados_efectivos integer NOT NULL DEFAULT 0,
  dias_falta_injustificada integer NOT NULL DEFAULT 0,
  horas_extras_diurnas_totales numeric NOT NULL DEFAULT 0.00,
  horas_extras_nocturnas_totales numeric NOT NULL DEFAULT 0.00,
  monto_salario_base_proporcional numeric NOT NULL,
  monto_ganancia_extras numeric NOT NULL DEFAULT 0.00,
  monto_deducciones_faltas numeric NOT NULL DEFAULT 0.00,
  monto_neto_pagar numeric NOT NULL,
  estado_reporte character varying NOT NULL DEFAULT 'BORRADOR'::character varying,
  generado_el timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  requiere_recalculo boolean DEFAULT false,
  CONSTRAINT reportes_prenomina_mensual_pkey PRIMARY KEY (id),
  CONSTRAINT fk_prenomina_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id),
  CONSTRAINT fk_prenomina_empleado FOREIGN KEY (empleado_id) REFERENCES public.empleados(id)
);
CREATE TABLE public.ultima_ubicacion (
  empleado_id uuid NOT NULL,
  latitud numeric NOT NULL,
  longitud numeric NOT NULL,
  precision_gps numeric,
  velocidad numeric,
  direccion numeric,
  estado_conexion character varying NOT NULL DEFAULT 'ACTIVO'::character varying CHECK (estado_conexion::text = ANY (ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying, 'DESCONECTADO'::character varying]::text[])),
  ultima_actualizacion timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
  empresa_id uuid,
  CONSTRAINT ultima_ubicacion_pkey PRIMARY KEY (empleado_id),
  CONSTRAINT fk_ultima_ubicacion_empleado FOREIGN KEY (empleado_id) REFERENCES public.empleados(id),
  CONSTRAINT ultima_ubicacion_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.historial_ubicaciones (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empleado_id uuid NOT NULL,
  latitud numeric NOT NULL,
  longitud numeric NOT NULL,
  precision_gps numeric,
  velocidad numeric,
  direccion numeric,
  registrado_en timestamp with time zone NOT NULL,
  creado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  empresa_id uuid,
  CONSTRAINT historial_ubicaciones_pkey PRIMARY KEY (id),
  CONSTRAINT fk_historial_empleado FOREIGN KEY (empleado_id) REFERENCES public.empleados(id),
  CONSTRAINT historial_ubicaciones_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.logs_auditoria_sistema (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empresa_id uuid NOT NULL,
  usuario_id uuid NOT NULL,
  rol_usuario character varying NOT NULL,
  accion character varying NOT NULL,
  tabla_afectada character varying NOT NULL,
  registro_id uuid,
  valor_anterior jsonb,
  valor_nuevo jsonb,
  direccion_ip character varying,
  creado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT logs_auditoria_sistema_pkey PRIMARY KEY (id),
  CONSTRAINT logs_auditoria_sistema_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id)
);
CREATE TABLE public.movimientos_vacaciones (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empresa_id uuid NOT NULL,
  empleado_id uuid NOT NULL,
  tipo_movimiento character varying NOT NULL,
  cantidad_dias integer NOT NULL,
  solicitud_id uuid,
  motivo_ajuste text,
  creado_por uuid,
  creado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT movimientos_vacaciones_pkey PRIMARY KEY (id),
  CONSTRAINT movimientos_vacaciones_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id),
  CONSTRAINT movimientos_vacaciones_empleado_id_fkey FOREIGN KEY (empleado_id) REFERENCES public.empleados(id),
  CONSTRAINT movimientos_vacaciones_solicitud_id_fkey FOREIGN KEY (solicitud_id) REFERENCES public.solicitudes_vacaciones(id)
);
CREATE TABLE public.registro_marcas (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  empresa_id uuid NOT NULL,
  empleado_id uuid NOT NULL,
  tipo_marca character varying NOT NULL,
  fecha_hora timestamp with time zone NOT NULL,
  modalidad character varying NOT NULL,
  foto_captura_url character varying,
  score_facial_coincidencia numeric,
  latitud numeric,
  longitud numeric,
  precision_gps_accuracy numeric,
  es_mock_location boolean DEFAULT false,
  creado_en timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT registro_marcas_pkey PRIMARY KEY (id),
  CONSTRAINT registro_marcas_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id),
  CONSTRAINT registro_marcas_empleado_id_fkey FOREIGN KEY (empleado_id) REFERENCES public.empleados(id)
);