# Historial de cambios

## Auditoría final de robustez de Etapa 2

- Se endureció la deserialización JSON para aceptar exponentes válidos y rechazar números mal formados, ceros iniciales y caracteres de control sin escapar.
- El cliente ahora rechaza respuestas exitosas incompletas y respuestas de error sin código o mensaje, en vez de mostrarlas como datos válidos.
- Se añadió validación defensiva del protocolo seleccionado y pruebas de regresión para todos estos escenarios.

## Historial de consultas en el cliente (Etapa 2)

- Se agregó un botón "Historial" que abre un diálogo con las consultas realizadas durante la sesión actual (hora, cédula, protocolo, éxito/error y un resumen del resultado), sin persistir nada en disco: se pierde al cerrar la aplicación.
- Nuevas clases `cliente.servicio.HistorialConsultas` (guarda hasta 50 entradas en memoria) y `cliente.servicio.EntradaHistorial`; nueva ventana `cliente.presentacion.VentanaHistorial`, con el mismo sistema de diseño ya usado en el resto del cliente.
- Al hacer clic en una entrada del historial se rellenan la cédula y el protocolo en el formulario principal; el usuario decide si vuelve a presionar "Consultar". El diálogo también permite limpiar el historial.
- No se agregó ninguna dependencia externa ni se modificaron `ServicioConsulta`, `ClienteTCP`/`ClienteHTTP` ni los DTO; `PruebaJson` y `PruebaServicioConsulta` se volvieron a ejecutar sin cambios y siguen pasando.

## Rediseño visual del cliente (Etapa 2)

- Se rediseñó por completo la apariencia de `VentanaPrincipal`, sin cambiar su arquitectura ni su comportamiento funcional: sigue delegando toda consulta en `ServicioConsulta` y no conoce detalles de red ni de JSON.
- Nuevas clases en `cliente.presentacion`: `Tema` (paleta de colores institucional, tipografía y espaciados centralizados), `BotonEstilizado` (botones pintados con `Graphics2D`, variantes primario/secundario/texto), `TarjetaPanel` (paneles tipo tarjeta con esquinas redondeadas), `PanelEstado` (banner de estado con franja de acento y barra de progreso para el estado de carga) y `CampoConMarcador` (campo de cédula con texto de ejemplo).
- La ventana pasó de tres bloques con `TitledBorder` genérico a un encabezado con título/subtítulo, una tarjeta de búsqueda y una tarjeta de resultado agrupada en "Datos personales" y "Datos territoriales", con un estado vacío ("—") antes de la primera consulta.
- La ventana ahora es responsiva (se puede redimensionar y el contenido se ajusta, con scroll vertical si la ventana es muy pequeña), en vez de depender de un tamaño mínimo fijo únicamente.
- `App` cambió del Look & Feel nativo del sistema operativo al multiplataforma (Metal) como base, para que los componentes propios de estilo se vean consistentes en cualquier sistema operativo.
- No se agregó ninguna dependencia externa ni se modificaron las capas de lógica (`ServicioConsulta`), comunicación (`ClienteTCP`/`ClienteHTTP`) ni DTO; `PruebaJson` y `PruebaServicioConsulta` se volvieron a ejecutar sin cambios y siguen pasando.

## Corrección de codificación real de distelec.txt

- Se detectó en pruebas de integración con el cliente que el servidor devolvía error 500 ("No fue posible leer los archivos del padron") con la excepción `MalformedInputException: Input length = 1` al consultar ciertas cédulas.
- Causa: `distelec.txt` viene en **ISO-8859-1**, no UTF-8, y sí incluye tildes y "ñ" reales (ej. código `119033` = "PEÑAS BLANCAS"), a diferencia de `PADRON.txt` que sustituye esos caracteres por "?". Leer el archivo como UTF-8 fallaba al llegar a la primera letra con tilde.
- Se corrigió `RepositorioDistritos` (y, por consistencia con la codificación real de origen, también `RepositorioPadron`) para leer con `StandardCharsets.ISO_8859_1` en vez de `UTF_8`. La salida hacia los clientes sigue siendo UTF-8 (sin cambios en `JsonUtil` ni en los servidores), verificado con los bytes exactos de `PEÑAS BLANCAS` (`0xC3 0x91` para la "Ñ" en la respuesta JSON).
- Se agregó un caso de prueba en `PruebaServicioPadron` que escribe un `distelec.txt` de prueba con una "Ñ" real en ISO-8859-1 y verifica que se decodifique correctamente.

## Etapa 2 — Cliente (construcción inicial)

- Nuevo proyecto independiente en `cliente/` (Ant/NetBeans, `main.class=cliente.App`): interfaz grafica Swing, sin dependencias externas.
- Arquitectura por capas: `VentanaPrincipal` (presentación) → `ServicioConsulta` (lógica, independiente del protocolo) → `ClienteTCP`/`ClienteHTTP` (comunicación) → `PersonaDTO`/`ErrorDTO`; `Configuracion` centraliza dirección/puertos/tiempo de espera y `Json` es un analizador JSON propio.
- Validación local de cédula antes de contactar al servidor; manejo de errores de comunicación (servidor no disponible, tiempo de espera agotado, JSON inválido, error informado por el servidor) sin cerrar la aplicación.
- Dirección del servidor configurable (no asume `localhost` de forma fija), permitiendo ejecutar cliente y servidor en computadoras distintas.
- Pruebas `PruebaJson` y `PruebaServicioConsulta` (con servidores TCP/HTTP de prueba propios, sin depender del servidor real).

## Corrección de formato real de PADRON.txt

- Se corrigió `RepositorioPadron` para reflejar el formato real del archivo `PADRON.txt` provisto: 7 campos (`CEDULA,CODELEC,FECHACADUC,JUNTA,NOMBRE,1.APELLIDO,2.APELLIDO`), no 8 como se había asumido inicialmente (no existe un campo "RELLENO" en la posición 3).
- El defecto anterior hacía que ninguna línea real alcanzara el mínimo de 8 columnas esperado, por lo que toda consulta devolvía "persona no encontrada" (404) aunque la cédula existiera en el padrón.
- Se actualizaron los datos de prueba de `PruebaServicioPadron` y `PruebasIntegracionServidor` para usar el formato real de 7 columnas.

## Auditoría técnica de Etapa 1

- Se reemplazó la construcción de JSON mediante concatenaciones extensas por un serializador estructurado con escape centralizado.
- El puerto TCP se enlaza antes de iniciar el hilo de aceptación para detectar errores de inicio correctamente.
- Se agregó manejo de rechazo del ejecutor y pruebas de puerto ocupado y caracteres especiales JSON.

## Etapa 1

- Servidores TCP y HTTP concurrentes con respuestas exclusivamente JSON.
- Consulta secuencial de PADRON.txt y distelec.txt mediante repositorios separados.
- Integración territorial, validaciones, errores controlados y tiempo máximo de lectura TCP.
- Pruebas de servicio, protocolos, rutas HTTP y concurrencia real.
- Documentación de configuración, arquitectura y uso de IA.
