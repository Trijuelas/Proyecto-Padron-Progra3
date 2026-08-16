# Historial de cambios

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
