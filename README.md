# Proyecto Padrón Electoral - Etapa 1

## Descripción y objetivo

Servidor Java 17 para consultar una persona del padrón electoral. Lee los archivos CSV originales `PADRON.txt` y `distelec.txt`, relaciona el código electoral y devuelve exclusivamente JSON. Esta etapa implementa el servidor; no incluye el cliente gráfico de la Etapa 2, base de datos ni XML.

## Tecnologías y requisitos

- Java 17 estándar y NetBeans (proyecto Ant).
- `java.net.ServerSocket` para TCP y `com.sun.net.httpserver.HttpServer` para HTTP.
- No requiere bibliotecas externas.
- Una copia local de `PADRON.txt` y `distelec.txt`. Los datos personales no se versionan ni se modifican.

## Estructura y arquitectura

```text
src/padron/
  modelo/          Persona, DistritoElectoral
  dto/             PersonaDTO, ErrorDTO
  repositorio/     RepositorioPadron, RepositorioDistritos
  servicio/        ServicioPadron, ResultadoConsulta
  presentacion/    ServidorTCP, ServidorHTTP
  config/          Configuracion
  util/            JsonUtil
  App.java         Punto de entrada
test/padron/       Pruebas ejecutables sin dependencias
docs/IA.md         Evidencia del uso de IA
```

Una consulta sigue este flujo:

```text
Cliente -> Servidor TCP o HTTP -> ServicioPadron -> RepositorioPadron
        -> RepositorioDistritos -> PersonaDTO/ErrorDTO -> JSON -> Cliente
```

Los servidores solo interpretan su protocolo y no leen archivos. `ServicioPadron` valida la cédula y coordina los repositorios; por ello se mantiene la separación de responsabilidades y ambos protocolos producen la misma respuesta.

## Configuración

Copie `config.properties.example` a `config.properties` en la raíz del proyecto y ajuste las rutas locales. No se debe versionar ese archivo.

```properties
tcp.port=5000
http.port=8080
padron.path=C:/datos/PADRON.txt
distritos.path=C:/datos/distelec.txt
worker.threads=12
```

También se aceptan propiedades del sistema o argumentos, por ejemplo:

```powershell
java -cp build/classes padron.App --padron.path='C:/datos/PADRON.txt' --distritos.path='C:/datos/distelec.txt'
```

## Ejecución

En NetBeans, abra el proyecto `Padron`, configure el archivo local `config.properties` y ejecute `padron.App`.

Desde PowerShell, en la raíz del proyecto:

```powershell
javac -encoding UTF-8 -d build/classes (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
java -cp build/classes padron.App
```

Al iniciar se habilitan TCP en el puerto 5000 y HTTP en el puerto 8080, salvo que se modifiquen en la configuración.

## Protocolos

### TCP

Envía una línea UTF-8:

```text
GET|115550555
```

Respuesta exitosa:

```json
{"cedula":"115550555","nombre":"JUAN","primerApellido":"PEREZ","segundoApellido":"RODRIGUEZ","codigoElectoral":"101001","provincia":"SAN JOSE","canton":"CENTRAL","distrito":"CARMEN"}
```

Los comandos desconocidos, solicitudes incompletas y cédulas inválidas devuelven JSON con código 400. Cada conexión tiene un límite de lectura de diez segundos para que un cliente que no envíe una línea completa no ocupe un hilo indefinidamente.

### HTTP

```text
GET /padron/115550555
```

Ejemplo: `http://localhost:8080/padron/115550555`. Todas las respuestas incluyen `Content-Type: application/json; charset=utf-8`.

## Errores

La forma de todo error es JSON:

```json
{"error":true,"codigo":404,"mensaje":"No se encontró una persona con la cédula indicada."}
```

| Situación | Código |
| --- | --- |
| Cédula vacía, inválida o solicitud TCP mal formada | 400 |
| Persona o ruta inexistente | 404 |
| Método HTTP distinto de GET | 405 (`Allow: GET`) |
| Tiempo de espera TCP | 408 |
| Archivo inexistente, error de lectura o error inesperado | 500 |

Las líneas incompletas de los datos se ignoran para preservar la disponibilidad. Los errores de una solicitud se encapsulan en una respuesta y no detienen los servidores.

## Concurrencia

`App` crea un único `ExecutorService` con un pool fijo configurable. `ServidorTCP` acepta conexiones y asigna cada cliente al pool; `HttpServer` usa el mismo pool para sus solicitudes. Así, varios clientes pueden ser procesados simultáneamente sin crear hilos sin límite ni bloquear permanentemente el servicio por una entrada TCP incompleta.

## Pruebas realizadas

Compile fuentes y pruebas:

```powershell
javac -encoding UTF-8 -d build/classes (Get-ChildItem -Recurse src,test -Filter *.java | ForEach-Object FullName)
java -cp build/classes padron.PruebaServicioPadron
java -cp build/classes padron.PruebasIntegracionServidor
```

`PruebaServicioPadron` verifica consulta correcta, relación territorial, inexistente, cédula inválida, líneas inválidas, archivo inexistente y escapado de caracteres especiales en JSON. `PruebasIntegracionServidor` inicia ambos servidores con archivos temporales y comprueba TCP válido/inválido, continuidad después de errores, HTTP válido, 404, 405, `Content-Type`, ruta mal formada, enlace TCP rechazado cuando el puerto está ocupado y doce clientes concurrentes (TCP y HTTP).

## Datos y privacidad

Los TXT originales se leen en modo consulta y nunca se modifican. Se mantienen fuera de Git mediante la ruta local en `config.properties`; no incluya PADRON.txt con información personal salvo instrucción institucional explícita.

## Uso de Inteligencia Artificial

Se documenta de forma transparente en `docs/IA.md`: el problema, prompt, propuesta recibida, partes utilizadas, descartadas y las adaptaciones aplicadas.
