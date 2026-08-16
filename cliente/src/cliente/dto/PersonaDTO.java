package cliente.dto;

/** Representa una persona del padron electoral tal como la entrega el servidor. */
public record PersonaDTO(String cedula, String nombre, String primerApellido, String segundoApellido,
        String codigoElectoral, String provincia, String canton, String distrito) {
}
