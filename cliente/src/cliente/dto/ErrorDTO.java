package cliente.dto;

/** Representa un error informado por el servidor o detectado localmente en el cliente. */
public record ErrorDTO(boolean error, int codigo, String mensaje) {
}
