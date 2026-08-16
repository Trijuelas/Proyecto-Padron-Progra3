package padron.repositorio;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import padron.modelo.Persona;
/** Consulta secuencialmente la fuente original; no usa base de datos ni la modifica. */
public final class RepositorioPadron {
    private final Path archivo;
    public RepositorioPadron(Path archivo) { this.archivo = archivo; }
    public Optional<Persona> buscarPorCedula(String cedula) throws IOException {
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] c = linea.split(",", -1);
                // Formato real de PADRON.txt: CEDULA,CODELEC,FECHACADUC,JUNTA,NOMBRE,1.APELLIDO,2.APELLIDO (7 campos)
                if (c.length < 7) {
                    continue;
                }
                if (cedula.equals(c[0].trim())) {
                    return Optional.of(new Persona(c[0].trim(), c[1].trim(), c[4].trim(),
                            c[5].trim(), c[6].trim()));
                }
            }
        }
        return Optional.empty();
    }
}
