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
