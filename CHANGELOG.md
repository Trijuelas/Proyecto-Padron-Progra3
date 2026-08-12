# Historial de cambios

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
