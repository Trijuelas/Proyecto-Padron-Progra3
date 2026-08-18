package cliente.servicio;

import cliente.dto.ErrorDTO;
import cliente.dto.PersonaDTO;

/** Resultado uniforme de una consulta, equivalente sin importar el protocolo utilizado. */
public record ResultadoConsulta(boolean exito, PersonaDTO persona, ErrorDTO error) {

    public static ResultadoConsulta deExito(PersonaDTO persona) {
        return new ResultadoConsulta(true, persona, null);
    }

    public static ResultadoConsulta deError(int codigo, String mensaje) {
        return new ResultadoConsulta(false, null, new ErrorDTO(true, codigo, mensaje));
    }
}
