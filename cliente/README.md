# Proyecto Padrón Electoral - Etapa 2 (Cliente)

## Descripción y objetivo

Aplicación cliente en Java 17 con interfaz gráfica (Swing, sin dependencias externas) que consume el servidor del Padrón Electoral desarrollado en la Etapa 1 (carpeta [`../`](../) en la raíz del repositorio). Permite ingresar una cédula, elegir el protocolo (TCP o HTTP), ejecutar la consulta y visualizar el resultado en campos identificados.

Este proyecto **no contiene ni accede** a `PADRON.txt` ni a `distelec.txt`: la única fuente de información es el servidor de la Etapa 1, consultado por red.

## Tecnologías y requisitos

- Java 17 estándar y NetBeans (proyecto Ant), igual que el servidor.
- `javax.swing` para la interfaz gráfica, `java.net.Socket`/`HttpURLConnection` para la comunicación.
- No requiere bibliotecas externas. El JSON se deserializa con un analizador propio (`cliente.util.Json`), sin usar `split`/`substring` sobre el texto recibido.
- El servidor de la Etapa 1 debe estar corriendo (en esta misma máquina o en otra, ver "Servidor remoto" más abajo).

## Estructura y arquitectura

```text
cliente/src/cliente/
  presentacion/   VentanaPrincipal (Swing; valida y delega, no comunica)
                  VentanaHistorial (dialogo con el historial de la sesion)
                  Tema, BotonEstilizado, TarjetaPanel, PanelEstado,
                  CampoConMarcador (sistema de diseno visual, sin dependencias externas)
  servicio/       ServicioConsulta, Protocolo, ResultadoConsulta (logica, independiente del protocolo)
                  HistorialConsultas, EntradaHistorial (historial en memoria, sin persistencia)
  comunicacion/   ClienteTCP, ClienteHTTP (solo transporte; no conocen JSON ni DTO)
  dto/            PersonaDTO, ErrorDTO
  config/         Configuracion (direccion y puertos del servidor, configurables)
  util/           Json (analizador JSON propio)
  App.java        Punto de entrada
cliente/test/cliente/   Pruebas ejecutables sin dependencias externas ni servidor real
```

Flujo de una consulta:

```text
Usuario -> VentanaPrincipal (valida localmente) -> ServicioConsulta
        -> ClienteTCP o ClienteHTTP (segun protocolo elegido) -> Servidor Etapa 1
        -> JSON recibido -> Json.analizarObjeto -> PersonaDTO/ErrorDTO -> VentanaPrincipal
```

`VentanaPrincipal` nunca abre sockets ni conexiones HTTP directamente: solo llama a `ServicioConsulta`, que es quien decide si delega en `ClienteTCP` o `ClienteHTTP`. Ambos caminos producen el mismo `ResultadoConsulta` para la capa superior, por lo que la interfaz no necesita conocer detalles de cada protocolo.

La consulta de red se ejecuta en un `SwingWorker` (hilo de fondo) para que la interfaz no se congele mientras se espera la respuesta del servidor.

## Configuración

Copie `config.properties.example` a `config.properties` en esta carpeta (`cliente/`) y ajuste según necesite. No se debe versionar ese archivo.

```properties
servidor.host=localhost
tcp.port=5000
http.port=8080
timeout.ms=8000
```

También se aceptan propiedades del sistema o argumentos, igual que en el servidor:

```powershell
java -cp build/classes cliente.App --servidor.host=192.168.1.10 --tcp.port=5000 --http.port=8080
```

Si no existe `config.properties` ni se pasan argumentos, el cliente usa por defecto `localhost:5000` (TCP) y `localhost:8080` (HTTP), pero la dirección **siempre es configurable** sin tocar código, para poder apuntar a un servidor en otra computadora.

## Ejecución

En NetBeans: abra el proyecto `PadronCliente` (esta carpeta), asegúrese de que el servidor de la Etapa 1 esté corriendo, configure `config.properties` si el servidor no está en `localhost`, y ejecute `cliente.App`.

Desde PowerShell, en esta carpeta:

```powershell
javac -encoding UTF-8 -d build/classes (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
java -cp build/classes cliente.App
```

## Servidor remoto

Como pide el enunciado, cliente y servidor pueden ejecutarse en computadoras distintas conectadas por red. Basta con configurar `servidor.host` (y los puertos, si son distintos de los valores por defecto) en `config.properties` apuntando a la dirección IP de la computadora donde corre el servidor de la Etapa 1. No es necesario modificar ninguna clase del cliente para esto.

## Manejo de errores

La interfaz nunca se cierra inesperadamente por un problema de comunicación. `ServicioConsulta` captura y traduce a un mensaje claro, entre otros: servidor no disponible, dirección incorrecta, tiempo de espera agotado, error de conexión, JSON inválido, y errores informados explícitamente por el servidor (por ejemplo cédula inexistente), sin presentarlos como si fueran una persona válida.

La validación local (cédula vacía o con formato inválido) ocurre antes de contactar al servidor; si falla, no se envía ninguna solicitud por red.

## Pruebas realizadas

Compile fuentes y pruebas:

```powershell
javac -encoding UTF-8 -d build/classes (Get-ChildItem -Recurse src,test -Filter *.java | ForEach-Object FullName)
java -cp build/classes cliente.PruebaJson
java -cp build/classes cliente.PruebaServicioConsulta
```

`PruebaJson` verifica el analizador JSON propio (cadenas, booleanos, números con exponente, `null`, escapado de caracteres especiales, arreglos y rechazo estricto de JSON inválido). `PruebaServicioConsulta` levanta servidores TCP y HTTP de prueba livianos (sin depender del proyecto del servidor real) y verifica: consulta TCP correcta, consulta HTTP correcta, cédula inexistente, validación local (no se envía nada al servidor si la cédula es inválida o no hay protocolo), servidor no disponible, JSON inválido y respuestas JSON incompletas.

### Evidencia manual (Etapa 1 + Etapa 2 integrados)

Además de las pruebas automatizadas, se verificó manualmente con el servidor real de la Etapa 1 corriendo con `PADRON_COMPLETO.txt`/`distelec.txt` reales:

- Consulta TCP correcta con una cédula existente: datos completos mostrados en la interfaz.
- Consulta HTTP correcta con la misma cédula: mismos datos mostrados.
- Cédula inexistente: mensaje de error controlado, sin mostrar datos.
- Servidor detenido: mensaje de error de conexión, la aplicación permanece funcional.

## Diseño visual

La interfaz usa una paleta de colores propia (azul institucional, tarjetas con esquinas redondeadas, banner de estado con indicador de carga) definida en `cliente.presentacion.Tema` y aplicada mediante componentes propios (`BotonEstilizado`, `TarjetaPanel`, `PanelEstado`, `CampoConMarcador`), pintados con `Graphics2D` estándar. No se usa ninguna biblioteca de estilos externa. La ventana es responsiva: se puede redimensionar y muestra una barra de desplazamiento vertical si el espacio disponible es menor al contenido.

## Historial de consultas

El botón "Historial" abre un diálogo con las consultas hechas durante la sesión actual (hora, cédula, protocolo y resultado). No se guarda en ningún archivo: se pierde al cerrar la aplicación. Al hacer clic en una entrada se rellenan la cédula y el protocolo en el formulario, sin volver a consultar automáticamente.

## Uso de Inteligencia Artificial

Se documenta de forma transparente en [`../docs/IA.md`](../docs/IA.md), en la sección correspondiente a la Etapa 2.
