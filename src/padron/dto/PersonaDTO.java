package padron.dto;
import padron.modelo.DistritoElectoral;
import padron.modelo.Persona;
public record PersonaDTO(String cedula, String nombre, String primerApellido, String segundoApellido, String codigoElectoral, String provincia, String canton, String distrito) {
    public static PersonaDTO de(Persona p, DistritoElectoral d) { return new PersonaDTO(p.cedula(), p.nombre(), p.primerApellido(), p.segundoApellido(), p.codigoElectoral(), d.provincia(), d.canton(), d.distrito()); }
}
