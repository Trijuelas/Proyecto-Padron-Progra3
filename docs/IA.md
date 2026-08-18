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

## Etapa 2 — Rediseño visual del cliente

### Problema

Una vez cerrada la parte funcional (servidor y cliente ya corregidos, probados e integrados en `main`), rediseñar la interfaz gráfica de `VentanaPrincipal` para que se vea moderna y profesional en vez de tener el aspecto por defecto de Swing (bordes con título genéricos, sin paleta de colores, sin jerarquía visual), preservando toda la funcionalidad y arquitectura existentes (capas, validación local, `SwingWorker`, manejo de errores).

### Prompt utilizado

> Quiero un rediseño visual y de UX completo de la interfaz del cliente: diseño moderno y profesional (no de apariencia académica), con jerarquía visual clara, tipografía, colores y espaciado cuidados, botones/controles con estados, agrupación tipo tarjeta para el resultado, manejo visible de estados de error y carga, y una ventana responsiva, sin perder ninguna funcionalidad existente y sin agregar dependencias externas.

Antes de aplicar cualquier cambio se presentó un diagnóstico de la interfaz existente por dimensión (layout, colores, espaciado, botones, estados de carga/error, componentes de resultado, responsividad) y se consultaron dos decisiones que no correspondía inventar: la dirección de paleta de colores (se eligió una paleta institucional costarricense: azul profundo como color primario, fondo gris neutro y acentos discretos de éxito/error) y el comportamiento de la ventana al redimensionar (se eligió una ventana responsiva en vez de tamaño fijo).

### Respuesta obtenida y partes utilizadas

Se propuso encapsular el sistema de diseño en clases nuevas del paquete `cliente.presentacion`, separadas de `VentanaPrincipal`: `Tema` (paleta de colores, tipografía y espaciados centralizados), `BotonEstilizado` (botón pintado con `Graphics2D`, con variantes primario/secundario/texto e independiente del Look & Feel del sistema operativo), `TarjetaPanel` (panel tipo tarjeta con esquinas redondeadas), `PanelEstado` (banner de estado con franja de acento y barra de progreso indeterminada para el estado de carga) y `CampoConMarcador` (campo de texto con texto de ejemplo/placeholder). Se usó esa estructura completa; `VentanaPrincipal` se reconstruyó para componerlas, con un encabezado con título/subtítulo, una tarjeta de búsqueda y una tarjeta de resultado agrupada en "Datos personales" y "Datos territoriales".

### Partes descartadas

Se descartó usar una biblioteca de estilos externa (por ejemplo FlatLaf) para no introducir una dependencia que el proyecto Ant/NetBeans no maneja y que el enunciado no permite. Se descartó usar el Look & Feel nativo del sistema operativo como base, porque Swing no permite recolorear de forma confiable sus botones nativos; se usó en su lugar el Look & Feel multiplataforma (Metal) como base neutra, combinado con componentes propios pintados a mano para el resto del estilo. Se descartó incluir imágenes o íconos externos (ningún archivo de imagen existía en el proyecto): el único elemento gráfico añadido (un monograma circular en el encabezado) se dibuja con `Graphics2D`, sin depender de ningún recurso externo. Se descartó atribuir el diseño a una identidad oficial del TSE, dejando el subtítulo como una descripción neutra del proyecto académico.

### Adaptaciones y revisión humana

- Se preservó exactamente la misma firma pública de `VentanaPrincipal(Configuracion)` y la misma delegación en `ServicioConsulta`/`ClienteTCP`/`ClienteHTTP`: el rediseño no toca las capas de lógica, comunicación ni DTO.
- Se mantuvo el comportamiento no editable/no enfocable de los campos de resultado (antes `JTextField` deshabilitados, ahora con el mismo estado pero estilo de "valor de tarjeta" en vez de campo de formulario), para no quitar ni cambiar el comportamiento existente sin necesidad.
- Se añadió un estado vacío explícito (guion largo "—" en los campos de resultado antes de la primera consulta) y un estado de carga visible (banner con texto y barra de progreso indeterminada), cubriendo los estados que el enunciado del rediseño pedía explícitamente.
- El campo de cédula ahora usa un texto de ejemplo (placeholder) que nunca se envía como valor real: se verificó explícitamente que `textoReal()` devuelve cadena vacía mientras se muestra el marcador, para no alterar la validación existente.
- Se compiló el proyecto completo y se volvieron a ejecutar `PruebaJson` y `PruebaServicioConsulta` (que no dependen de la interfaz gráfica) para confirmar que la lógica no cambió; la verificación visual final se hizo ejecutando el cliente real desde NetBeans/PowerShell, igual que en las etapas anteriores.

## Etapa 2 — Historial de consultas

### Problema

Agregar al cliente una opción de historial que muestre las consultas ya realizadas durante la sesión, para no tener que volver a escribir la misma cédula si se quiere repetir o revisar una consulta anterior.

### Prompt utilizado

> Me gustaría agregarle una opción de historial de consultas, ¿crees factible hacer ese ajuste?

Antes de programar nada se explicó por qué es factible sin romper ninguna regla del proyecto (sin base de datos, sin dependencias externas) y se consultaron tres decisiones que no correspondía inventar: dónde mostrar el historial en la interfaz (se eligió una ventana aparte, abierta con un botón nuevo), si debía persistir entre ejecuciones del programa o solo durante la sesión actual (se eligió solo la sesión actual, sin guardar nada en disco) y qué debía pasar al hacer clic en una entrada (se eligió que solo rellene el formulario, sin volver a consultar automáticamente).

### Respuesta obtenida y partes utilizadas

Se propuso mantener la separación de capas ya existente: una clase de lógica nueva en `cliente.servicio` (`HistorialConsultas`, con un límite de 50 entradas en memoria y sin persistencia) y un registro simple (`EntradaHistorial`, con hora, cédula, protocolo, éxito/error y un resumen del resultado); `VentanaPrincipal` solo registra una entrada después de cada consulta completada y le pasa el historial a un nuevo diálogo (`VentanaHistorial`) que lo muestra, sin que ninguno de los dos conozca `ServicioConsulta` directamente. Se usó esa estructura completa.

### Partes descartadas

Se descartó guardar el historial en un archivo local (aunque era técnicamente viable sin bases de datos ni XML), porque el usuario prefirió no persistir cédulas consultadas en disco entre ejecuciones. Se descartó integrar el historial como panel fijo dentro de la ventana principal o como pestaña, para no alargar ni reestructurar la pantalla ya rediseñada; se prefirió un diálogo aparte, consistente con el resto del sistema de diseño (`Tema`, `TarjetaPanel`, `BotonEstilizado`). Se descartó repetir la consulta automáticamente al hacer clic en una entrada, dejando esa decisión en manos del usuario tras revisar el formulario ya rellenado.

### Adaptaciones y revisión humana

- Solo se registran en el historial las consultas que efectivamente se enviaron y respondieron (éxito o error del servidor); las validaciones locales que nunca llegan a contactar al servidor no generan una entrada.
- Se agregó `CampoConMarcador.establecerTexto(...)` para poder rellenar el campo de cédula desde el historial sin confundir ese valor con el texto de ejemplo (marcador).
- Se compiló el proyecto completo y se volvieron a ejecutar `PruebaJson` y `PruebaServicioConsulta`, que siguen pasando sin cambios; la verificación visual del nuevo diálogo se hace ejecutando el cliente real.

## Auditoría final de requisitos de Etapas 1 y 2

### Problema y prompt utilizado

Se solicitó contrastar el servidor, el cliente y un ZIP de referencia contra ambos enunciados oficiales, corregir cualquier defecto que afectara la rúbrica y actualizar el repositorio únicamente después de verificar la solución.

### Resultado utilizado y decisiones

La auditoría confirmó que la estructura integrada del repositorio era más completa y limpia que el ZIP, que incluía binarios y una copia duplicada del servidor. Se conservó la implementación del repositorio y se corrigieron dos riesgos concretos: el analizador JSON no implementaba estrictamente la gramática numérica y `ServicioConsulta` aceptaba como persona válida un objeto con campos ausentes. También se agregó validación defensiva de protocolo nulo.

### Cambios y verificación humana

Se añadieron pruebas para exponentes válidos, números y cadenas inválidos, respuestas incompletas y ausencia de protocolo. Después se recompilaron por separado servidor y cliente con Java 17 y se ejecutaron las cuatro suites disponibles: servicio del padrón, integración TCP/HTTP y concurrencia, analizador JSON y servicio de consulta. Todas finalizaron correctamente.
