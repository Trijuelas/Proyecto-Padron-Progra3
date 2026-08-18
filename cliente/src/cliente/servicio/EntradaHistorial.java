package cliente.servicio;

import java.time.LocalTime;

/**
 * Una consulta ya realizada durante la sesion actual, para mostrarla en el
 * historial. No se persiste en disco: vive solo en memoria mientras la
 * aplicacion esta abierta.
 */
public record EntradaHistorial(LocalTime hora, String cedula, Protocolo protocolo, boolean exito, String resumen) {
}
