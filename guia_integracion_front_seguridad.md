# Guía de Integración Frontend: Seguridad, Biometría y Geolocalización (RF23 a RF31)

Este documento describe el plan técnico para que el equipo de frontend (`front-clara`) integre los nuevos controles de seguridad y biometría en el ponche de asistencia y el panel de monitoreo administrativo.

---

## 1. Actualización de Contratos API (Payloads)

Al llamar al endpoint de marcación diaria:
`POST /api/v1/empleados/asistencia` (o el endpoint correspondiente configurado para empleados).

### Payload de Petición (Request Body)
El objeto JSON enviado por el cliente ahora debe incluir las métricas y validaciones de hardware capturadas:

```json
{
  "tipoMarcacion": "ENTRADA",       // Opciones: "ENTRADA", "ALMUERZO", "SALIDA"
  "origenMarcacion": "BOTON_REMOTO", // Opciones: "QR_FISICO", "QR_DINAMICO", "BOTON_REMOTO"
  "tokenQr": null,                  // Obligatorio solo si origenMarcacion es "QR_DINAMICO"
  
  // --- NUEVOS CAMPOS DE SEGURIDAD (Obligatorios en modalidad REMOTA) ---
  "esFacialVerificado": true,       // Confirmación de que el SDK facial analizó la foto
  "precisionGpsAccuracy": 14.50,    // Precisión en metros provista por el sensor GPS
  "latitud": 4.6097123,             // Latitud capturada del sensor GPS
  "longitud": -74.0817543,          // Longitud capturada del sensor GPS
  "esMockLocation": false,          // Bandera que indica si el GPS es simulado/alterado
  "fotoCapturaUrl": "https://bucket-s3.s3.amazonaws.com/asistencias/carlos_gomez_entrada.jpg", // Foto en S3
  "scoreFacialCoincidencia": 94.85  // Porcentaje de coincidencia devuelto por el SDK facial (Min 80%)
}
```

### Respuestas del Servidor

*   **200 OK (Registro Exitoso):**
    Retorna los datos persistidos de la marcación junto con el mensaje de éxito.
*   **400 Bad Request (Fallo de Validación / Fraude Detectado):**
    Si falla alguna validación, el backend denegará el ponche y responderá con código `400`. El cuerpo de respuesta contendrá la descripción del error para mostrar al empleado (ej: *"El dispositivo se encuentra fuera de la geocerca permitida."*).

---

## 2. Flujo de Trabajo en el Dispositivo del Empleado (Autogestión)

### A. Marcación Remota (`BOTON_REMOTO`)
Cuando el empleado haga clic en el botón de asistencia remota:

1.  **Solicitar Permisos:** Verificar accesos a la cámara y geolocalización.
2.  **Validar GPS y Detección de Simulación (Mock Location):**
    *   Obtener las coordenadas usando la API de geolocalización del navegador o del dispositivo móvil.
    *   **Detección de Mock Locations:**
        *   En navegadores web: El API de HTML5 no proporciona una bandera nativa de "mock", pero se puede inferir si la precisión (`accuracy`) es sospechosamente perfecta (ej: exactamente `0` o `1` metro en interiores) o si se ejecuta en entornos de desarrollo.
        *   En aplicaciones híbridas (Capacitor/React Native): Se debe usar un plugin nativo (como `cordova-plugin-geolocation` o `react-native-geolocation-service`) que evalúe si la ubicación proviene de un proveedor simulado (`mocked` o `isFromMockProvider` es true).
3.  **Captura y Verificación Biométrica Facial:**
    *   Activar la cámara y capturar una selfie en alta definición.
    *   Subir la fotografía al bucket de AWS S3 correspondiente para almacenar el ponche físico.
    *   **Procesamiento Facial (AWS Rekognition / SDK Local):**
        *   *Simulación local:* Durante la etapa actual de desarrollo offline, el cliente simula la llamada y calcula una puntuación aleatoria `>= 80%` comparada con la foto de perfil patrón cargada en el perfil del empleado.
        *   El cliente debe setear `esFacialVerificado = true` y el `scoreFacialCoincidencia` respectivo.
4.  **Despachar Ponche:** Enviar la información completa en el JSON del `POST`.

### Código de Ejemplo en React (Captura GPS y Mock Check)

```javascript
const handleRemoteClockin = async () => {
  if (!navigator.geolocation) {
    alert("La geolocalización no está soportada por este navegador.");
    return;
  }

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const { latitude, longitude, accuracy, mock } = position.coords;
      
      // Detección básica de mock location en navegadores (castear banderas específicas si existen)
      const esMock = mock || position.mocked || accuracy === 0;

      // 1. Tomar selfie con la webcam/cámara
      const fotoUrl = await tomarFotoYSubirAS3(); 
      
      // 2. Simular comparación biométrica facial
      const score = 92.5; // Simulación del Rekognition Match Score

      const payload = {
        tipoMarcacion: "ENTRADA",
        origenMarcacion: "BOTON_REMOTO",
        tokenQr: null,
        esFacialVerificado: true,
        precisionGpsAccuracy: accuracy,
        latitud: latitude,
        longitud: longitude,
        esMockLocation: esMock,
        fotoCapturaUrl: fotoUrl,
        scoreFacialCoincidencia: score
      };

      try {
        const response = await api.post("/api/v1/empleados/asistencia", payload);
        alert("¡Asistencia registrada con éxito!");
      } catch (error) {
        // Manejo estructurado de fallas (FACE_MISMATCH, MOCK_LOCATION, FUERA_DE_GEOCERCA)
        alert(`Error al registrar asistencia: ${error.response.data.mensaje}`);
      }
    },
    (err) => alert(`Error de GPS: ${err.message}`),
    { enableHighAccuracy: true }
  );
};
```

### B. Marcación Presencial con QR Dinámico (`QR_DINAMICO`)
*   El scanner de la recepción de la oficina mostrará un código QR que cambia dinámicamente.
*   **Formato del QR generado:** `QR_{empresaId}_{timestampSeconds}` (ej: `QR_550e8400-e29b-41d4-a716-446655440000_1780435200`).
*   **Regla de Expiración (5 segundos):** El servidor del backend rechazará cualquier ponche cuyo timestamp en el token QR difiera en más de 5 segundos con el instante del reloj del servidor.
*   **Acción del Front:** Una vez escaneado el código QR, se debe enviar inmediatamente el token en la petición del ponche. No almacene en caché ni retrase la transmisión.

---

## 3. Integración en el Panel de RRHH (Monitoreo Administrativo)

### Recepción de Alertas de Seguridad en Vivo vía WebSockets
El backend publica alertas de anomalías graves de forma asíncrona a través del canal de WebSockets:
`ws://[servidor]/ws/ubicaciones`.

El frontend de administración debe suscribirse a esta conexión. Cuando se detecte una anomalía grave en el control de acceso, el socket recibirá un evento con el payload `ANOMALIA_GRAVE`.

#### Formato del Mensaje Recibido (Event Payload)
```json
{
  "event": "ANOMALIA_GRAVE",
  "id": "78fa29cc-11ef-417d-8153-f725a3d76e73",
  "empleadoId": "a90df2cd-b516-43d9-952b-42fa600a74aa",
  "empleadoNombre": "Carlos Gomez",
  "tipoAnomalia": "MOCK_LOCATION_DETECTADA", // Opciones: 'MOCK_LOCATION_DETECTADA', 'FACE_MISMATCH', 'FUERA_DE_GEOCERCA'
  "detallesTecnicos": "GPS simulado detectado para empleado Carlos Gomez (ID: a90df2cd-b516-43d9-952b-42fa600a74aa). Coordenadas enviadas: Lat=4.6097, Lon=-74.0817. Origen: BOTON_REMOTO",
  "creadoEn": "2026-06-02T16:20:05.123-05:00"
}
```

### Comportamiento Visual Recomendado en el Mapa de Monitoreo
Al recibir este evento en la bandeja administrativa:
1.  **Notificación Inmediata:** Mostrar un Toast de alerta crítica de color rojo (`#ef4444`) con sonido discreto indicando la anomalía y el nombre del empleado.
2.  **Interacción con el Mapa:**
    *   Si el mapa está abierto, buscar al empleado.
    *   Reemplazar su marcador temporalmente por un ícono de advertencia rojo parpadeante (ej: usando clases CSS de animación de pulso).
    *   Al hacer clic en el marcador o en la bandeja, abrir un modal con la información técnica de la auditoría y un enlace directo a la foto capturada en S3 (especialmente útil para investigar el error `FACE_MISMATCH`).

```javascript
// Ejemplo de conexión en React Hook para el Dashboard RRHH
useEffect(() => {
  const socket = new WebSocket("ws://localhost:8080/ws/ubicaciones");

  socket.onmessage = (event) => {
    const data = JSON.parse(event.data);
    
    if (data.event === "ANOMALIA_GRAVE") {
      // Activar Toast Crítico
      showCriticalToast({
        title: `¡Anomalía Grave: ${data.tipoAnomalia}!`,
        description: `Empleado: ${data.empleadoNombre}. Detalles: ${data.detallesTecnicos}`,
        color: "red"
      });
      
      // Opcional: Centrar mapa y animar marcador
      resaltarMarcadorEmpleado(data.empleadoId);
    }
  };

  return () => socket.close();
}, []);
```
