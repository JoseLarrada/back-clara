# Plan de implementación AWS - CloudTime v1

## Objetivo

Integrar servicios AWS de forma incremental para soportar almacenamiento, notificaciones y procesamiento asíncrono sin romper la arquitectura actual del backend Spring Boot.

## Estado actual

- Backend Spring Boot 4.0.6 + Java 21
- PostgreSQL multi-tenant sobre RDS normal
- JWT RSA
- Módulos funcionales ya modelados: Empleados, Vacaciones, Justificaciones, ReglasHorario, Pre-Nómina
- Configuración base con `.env` en `application.yml`
- **Sin integración AWS todavía**

## Principio rector

Primero habilitar infraestructura y contratos de integración; después conectar los servicios del backend por prioridad funcional.

---

## Fase 0 - Definición de fundamentos

**Objetivo:** dejar lista la base técnica y de infraestructura.

### Tareas

- Definir región AWS y naming estándar.
- Definir variables de entorno por ambiente.
- Establecer estrategia multi-tenant en AWS por empresa o por bucket/prefix según el caso.
- Confirmar que la red, balanceadores y RDS ya quedan fuera de este alcance porque ya están resueltos.
- Definir si toda la infraestructura nueva se gestionará con Terraform.
- Decidir si usaremos AWS SDK v2 o integración Spring sobre servicios específicos.

### Entregables

- Convención de nombres.
- Variables `.env` documentadas.
- Decisión de IaC.

---

## Fase 1 - S3 para archivos y evidencia documental

**Objetivo:** almacenar comprobantes, soportes, reportes y archivos generados.

### Avance funcional actual (sin S3 aun)

- RF20 ya permite que el empleado solicite justificacion con `urlComprobanteS3`.
- RF21 ya permite que el empleado consulte su saldo y envie solicitud de vacaciones.
- Pendiente de Fase 1: reemplazar el uso de URL manual por flujo real de upload/download en S3 con URL firmada.

### Casos de uso prioritarios

- Justificaciones de incidencias
- Comprobantes de vacaciones / anexos
- Exportaciones de pre-nómina (CSV, Excel, PDF)
- Archivos auxiliares de auditoría

### Cambios sugeridos

- Crear un servicio de almacenamiento, por ejemplo `StorageService`.
- Crear cliente S3 con configuración centralizada.
- Guardar metadatos en BD: bucket, key, mime-type, tamaño, checksum, entidad origen.
- Implementar descarga segura con validación de tenant.

### Impacto en backend

- `Backups_Incidencias`
- `Vacaciones`
- `Reportes_Prenomina`

### Resultado esperado

- El sistema ya no dependerá de archivos locales.
- Los administrativos podrán acceder a soportes históricos.

---

## Fase 2 - SNS para eventos y notificaciones

**Objetivo:** notificar eventos de negocio relevantes.

### Eventos candidatos

- Nueva solicitud de vacaciones
- Vacación aprobada o rechazada
- Justificación aprobada o rechazada
- Incidencia grave detectada
- Reporte mensual generado

### Cambios sugeridos

- Crear un publicador de eventos SNS.
- Definir tópicos por dominio o uno central con atributos.
- Registrar payloads mínimos y auditables.

### Impacto en backend

- `VacacionesServiceImpl`
- `BackupIncidenciasServiceImpl`
- `ReportesPrenominaMensualServiceImpl`

### Resultado esperado

- Los administrativos reciben alertas y trazabilidad.
- Se facilita integrar correo, Slack, SMS o workflows externos después.

---

## Fase 3 - SQS para tareas asíncronas

**Objetivo:** desacoplar procesos pesados y evitar bloquear al usuario.

### Casos de uso candidatos

- Generación masiva de pre-nómina
- Exportaciones grandes
- Procesamiento de soportes
- Reintentos de notificación

### Cambios sugeridos

- Crear colas por tipo de proceso o una cola central con tipo de evento.
- Implementar consumidor asíncrono en backend o lambda según convenga.
- Manejo de reintentos y DLQ.

### Resultado esperado

- Menor tiempo de respuesta en APIs críticas.
- Tareas pesadas fuera del flujo síncrono.

---

## Fase 4 - Lambda para lógica puntual (Opcional - Futuro)

**Objetivo:** mover lógica muy específica o event-driven fuera del backend principal cuando convenga.

### Casos de uso candidatos

- Procesamiento de archivos subidos a S3
- Validaciones automáticas de soportes
- Generación de artefactos simples
- Procesos disparados por eventos SNS/SQS

### Resultado esperado

- Menor carga al backend principal.
- Más escalabilidad por función.

---

## Arquitectura de ejecución (Fase Local / Híbrida)

**Objetivo:** Probar la integración AWS desde el backend local antes de subir a la nube.

### Configuración Actual

- Backend Spring Boot corre localmente.
- Se conecta a recursos reales de AWS (S3/SNS) usando credenciales de IAM.
- El RDS puede ser local o remoto según disponibilidad.

### Próximos pasos de despliegue (Postergados)

- ECS Cluster + Services ( Dockerización pendiente).
- Pipeline CI/CD.

---

## Fase 5 - Integración con frontend y experiencia administrativa

**Objetivo:** que el valor llegue al usuario final.

### Casos de uso

- Botón de subir comprobante
- Vista de historial de archivos
- Bandeja de notificaciones
- Descarga de exportaciones
- Estado de procesamiento de reportes

---

## Mapa recomendado por módulo actual

### `Backups_Incidencias`

- **S3:** subir y guardar comprobantes
- **SNS:** notificar cuando se crea o revisa una justificación
- **SQS/Lambda:** procesar archivos o validaciones si crece el volumen

Estado actual:

- Flujo de justificación por empleado implementado a nivel API.
- En la integración AWS se conectará a S3 para reemplazar adjunto por URL cargada manualmente.

### `Vacaciones`

- **SNS:** aviso de solicitud/aprobación/rechazo
- **S3:** soportes opcionales y evidencias

Estado actual:

- Solicitud de vacaciones por empleado y consulta de saldo ya implementadas en backend.
- Con AWS se añadirá publicación SNS para eventos de solicitud/aprobación/rechazo.

### `Reportes_Prenomina`

- **S3:** almacenar exportaciones y snapshots
- **SQS:** generación de reportes grandes en background
- **SNS:** notificar cuando el reporte esté listo

### `RegistrosAsistencias`

- **SNS:** eventos anómalos o incidencia crítica
- **S3:** evidencia asociada a fraude o auditoría

---

## Propuesta de configuración técnica

### Variables de entorno sugeridas

- `AWS_REGION`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_S3_BUCKET_NAME`
- `AWS_SNS_TOPIC_ARN`
- `AWS_SQS_QUEUE_URL`
- `AWS_SQS_DLQ_URL`
- `AWS_ACCOUNT_ID`

### Archivos a crear cuando implementemos

- `src/main/java/.../config/AwsConfig.java`
- `src/main/java/.../config/S3StorageConfig.java`
- `src/main/java/.../config/SnsConfig.java`
- `src/main/java/.../config/SqsConfig.java`
- `src/main/java/.../Features/**/service/StorageService.java`
- `src/main/java/.../Features/**/service/EventPublisher.java`

### Lo que NO entra en este alcance

- Gestión de IAM desde el código o desde este plan.
- Red/VPC/ALB/RDS porque ya los tienes resueltos.
- Migración a una base de datos diferente; se mantiene PostgreSQL en RDS normal.

---

## Orden recomendado de implementación real

1. S3 para archivos y soportes
2. SNS para eventos de negocio
3. SQS para procesos asíncronos
4. Lambda para tareas puntuales
5. Frontend para consumo de artefactos y alertas
6. ECS task definitions para empaquetar y desplegar backend/frontend

---

## Reglas de decisión antes de codificar

Antes de tocar el backend, confirmar:

- Qué archivos deben ir a S3 y cuáles no.
- Si cada empresa tendrá prefijo propio o bucket compartido.
- Si SNS será uno solo o por dominio.
- Si SQS será centralizada o por módulo.
- Si la exportación de pre-nómina debe ser síncrona o asíncrona.

---

## Próximo paso sugerido

Si te parece, el siguiente paso es definir juntos el **alcance exacto de la Fase 1**:

- qué tipos de archivos subirán a S3,
- qué metadata se guardará en BD,
- y si los soportes serán públicos, privados o con URL firmada.
