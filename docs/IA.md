# Uso de Inteligencia Artificial

## Problema

Implementar la Etapa 1 del servidor de consulta del padrón electoral en Java: lectura de archivos CSV, integración territorial, respuestas JSON por TCP y HTTP, validación y concurrencia.

## Prompt utilizado

> Quiero que desarrolles completamente la Etapa 1 del proyecto de Programación III “Servidor de Consulta del Padrón Electoral”. El servidor debe consultar PADRON.txt y distelec.txt, responder JSON por TCP (`GET|cedula`) y HTTP (`GET /padron/{cedula}`), mantener entidades, DTO, repositorios, servicio y presentación separados, usar concurrencia y documentar pruebas y Git.

## Respuesta obtenida y partes utilizadas

La IA propuso una arquitectura por capas con entidades, DTO, repositorios, un servicio central, dos adaptadores de protocolo y un `ExecutorService` compartido. Se usó esa orientación para organizar el código, definir los casos de error JSON y crear pruebas ejecutables sin dependencias externas.

## Partes descartadas

Se descartaron sugerencias de usar base de datos, XML, frameworks web, dependencias externas y cliente gráfico, porque no corresponden a esta etapa ni al nivel del proyecto.

## Adaptaciones y revisión humana

- Se ajustó el lector de `PADRON.txt` al orden real de ocho campos: la posición 4 es JUNTA y el nombre comienza en la posición 5.
- Se mantuvo la lectura secuencial de los TXT, sin modificar archivos fuente.
- Se implementó serialización JSON limitada a los DTO concretos del proyecto y escapado de caracteres de control.
- Se añadieron límites de lectura TCP y pruebas reales de TCP, HTTP y múltiples clientes simultáneos.
- La propuesta fue revisada para conservar nombres claros, Java estándar y responsabilidades separadas.

## Auditoría posterior

En una revisión posterior se solicitó auditar la implementación real de la Etapa 1, ejecutar las pruebas de TCP, HTTP y concurrencia, revisar la seguridad de la serialización JSON y corregir cualquier incumplimiento antes de integrar la rama. A partir de esa solicitud se sustituyeron las concatenaciones extensas del serializador por métodos estructurados, se validó el enlace del puerto TCP antes de anunciar el inicio y se ampliaron las pruebas de JSON y de puerto ocupado. No se utilizaron frameworks ni se desarrolló funcionalidad de la Etapa 2.

## Corrección de formato de PADRON.txt

Al comparar el código contra el archivo `PADRON.txt` real (revisado con muestras del archivo provisto), se detectó que `RepositorioPadron` asumía 8 campos por línea con un campo "RELLENO" que en realidad no existe: el archivo real trae 7 campos (`CEDULA,CODELEC,FECHACADUC,JUNTA,NOMBRE,1.APELLIDO,2.APELLIDO`). Con la validación anterior (`c.length < 8`), ninguna línea real cumplía el mínimo de columnas, por lo que toda consulta devolvía "persona no encontrada" sin ningún error visible. Se corrigieron los índices de columnas y el mínimo de longitud en `RepositorioPadron`, y se actualizaron los datos de prueba de `PruebaServicioPadron` y `PruebasIntegracionServidor` para reflejar el formato real de 7 columnas.

Como parte de esta misma revisión se verificó también la codificación de caracteres: se confirmó contra los bytes reales de `PADRON_COMPLETO.txt` que el archivo es UTF-8 válido, y que casos como "ACU?A" (en vez de "ACUÑA") ya vienen así en el dato de origen del TSE (un carácter `?` literal, no una "Ñ" mal decodificada). Por lo tanto no se modificó la codificación de lectura (`StandardCharsets.UTF_8`) de `PADRON.txt`, que ya era correcta para ese archivo.

### Corrección posterior: distelec.txt sí necesitaba otra codificación

Esa verificación de codificación se hizo revisando solo una muestra parcial (los primeros ~20 KB) de `distelec.txt`, no el archivo completo. Al integrar y probar el cliente de la Etapa 2 de punta a punta, apareció un error real (`MalformedInputException: Input length = 1`) al consultar ciertas cédulas. Al revisar el archivo `distelec.txt` completo (178 KB, sí es manejable entero) se encontró que, a diferencia de `PADRON.txt`, sí contiene tildes y "ñ" reales en codificación ISO-8859-1 (ejemplo confirmado: código `119033` = "PEÑAS BLANCAS", byte `0xD1` para la "ñ"), en una zona del archivo posterior a la muestra revisada originalmente. Se corrigió `RepositorioDistritos` (y `RepositorioPadron`, por consistencia) para leer en ISO-8859-1, y se verificó con los bytes reales del archivo completo y con un caso de prueba dedicado. Ver la entrada correspondiente en `CHANGELOG.md`.

## Etapa 2 — Cliente

### Problema

Implementar la Etapa 2 del proyecto: una aplicación cliente en Java con interfaz gráfica que consuma el servidor de la Etapa 1 mediante TCP y HTTP, deserialice JSON, valide localmente antes de enviar solicitudes, maneje errores de comunicación sin cerrarse inesperadamente, y permita configurar un servidor remoto (no asumir siempre `localhost`).

### Prompt utilizado

> Quiero que construyas desde cero la Etapa 2 del proyecto de Programación III ("Cliente de Consulta del Padrón Electoral"): una aplicación cliente en Java con interfaz gráfica (sin consola como interfaz principal) que consuma el servidor de la Etapa 1 por TCP (`GET|cedula`) y HTTP (`GET /padron/{cedula}`), con selección de protocolo, deserialización JSON propia (sin split/substring), separación de capas (presentación, lógica, comunicación, DTO, utilidades), manejo de errores robusto, dirección de servidor configurable, y pruebas ejecutables sin depender del servidor real.

### Respuesta obtenida y partes utilizadas

Se propuso una arquitectura por capas equivalente a la del servidor: `VentanaPrincipal` (Swing) que solo valida y delega, `ServicioConsulta` como lógica independiente del protocolo, `ClienteTCP`/`ClienteHTTP` como capa de comunicación pura (sin conocer JSON ni DTO), `PersonaDTO`/`ErrorDTO` como resultado, `Configuracion` centralizando dirección/puertos/tiempo de espera, y un analizador JSON propio (`cliente.util.Json`) para deserializar sin dependencias externas. Se usó esa estructura completa.

### Partes descartadas

Se descartó usar una biblioteca JSON externa (Gson/Jackson) para mantener la coherencia con la Etapa 1 ("no requiere bibliotecas externas") y evitar depender de una gestión de dependencias que el proyecto Ant/NetBeans no tenía configurada. Se descartó JavaFX como toolkit de interfaz por no venir incluido en el JDK estándar desde la versión 11, prefiriendo Swing. Se descartó implementar la comunicación directamente en los eventos de los botones de la GUI, por estar explícitamente prohibido en el enunciado.

### Adaptaciones y revisión humana

- Se ejecutó la consulta de red en un `SwingWorker` (hilo de fondo) para que la interfaz no se congele mientras se espera respuesta del servidor, algo no mencionado explícitamente en el prompt pero necesario dado que el servidor puede tardar unos segundos en un archivo de 422 MB.
- Se revisaron y ordenaron los `catch` de `ServicioConsulta` de más específico a más general (`SocketTimeoutException`, `UnknownHostException`, `ConnectException`, `IOException`) para dar mensajes de error claros y distintos según el problema real.
- Se verificó manualmente que el cliente funciona de punta a punta contra el servidor real corregido (Etapa 1) antes de dar por cerrada la etapa.

### Auditoría y pruebas

Se compilaron y ejecutaron `PruebaJson` (analizador JSON: cadenas, booleanos, números, `null`, escapado, arreglos, JSON inválido) y `PruebaServicioConsulta` (contra servidores TCP/HTTP de prueba livianos y propios, sin depender del proyecto del servidor real): consulta TCP correcta, consulta HTTP correcta, cédula inexistente, validación local, servidor no disponible y JSON inválido. Todas las pruebas pasaron antes de considerar la etapa lista para revisión.
