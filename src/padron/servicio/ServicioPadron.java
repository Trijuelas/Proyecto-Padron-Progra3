package padron.servicio;
import java.io.IOException;
import padron.dto.ErrorDTO;
import padron.dto.PersonaDTO;
import padron.modelo.Persona;
import padron.repositorio.RepositorioDistritos;
import padron.repositorio.RepositorioPadron;
public final class ServicioPadron {
    private final RepositorioPadron padron; private final RepositorioDistritos distritos;
    public ServicioPadron(RepositorioPadron padron, RepositorioDistritos distritos) { this.padron = padron; this.distritos = distritos; }
    public ResultadoConsulta consultar(String cedula) {
        if (cedula == null || !cedula.matches("\\d{9}")) return error(400, "La cedula debe contener exactamente 9 digitos.");
        try {
            Persona persona = padron.buscarPorCedula(cedula).orElse(null);
            if (persona == null) return error(404, "No se encontro una persona con la cedula indicada.");
            return distritos.buscarPorCodigo(persona.codigoElectoral()).<ResultadoConsulta>map(d -> new ResultadoConsulta(200, PersonaDTO.de(persona, d))).orElseGet(() -> error(404, "No se encontro la division territorial asociada a la persona."));
        } catch (IOException e) { return error(500, "No fue posible leer los archivos del padron."); }
    }
    public ResultadoConsulta error(int codigo, String mensaje) { return new ResultadoConsulta(codigo, new ErrorDTO(true, codigo, mensaje)); }
}
