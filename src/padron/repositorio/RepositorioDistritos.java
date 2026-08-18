package padron.repositorio;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import padron.modelo.DistritoElectoral;
public final class RepositorioDistritos {
    private final Path archivo;
    public RepositorioDistritos(Path archivo) { this.archivo = archivo; }
    public Optional<DistritoElectoral> buscarPorCodigo(String codigo) throws IOException {
        // El TSE distribuye distelec.txt en ISO-8859-1: trae tildes y enies reales (ej. "PENAS BLANCAS"
        // con enie), a diferencia de PADRON.txt que sustituye esos caracteres por "?". Leerlo como
        // UTF-8 falla (MalformedInputException, "Input length = 1") al llegar a la primera letra con tilde.
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.ISO_8859_1)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] c = linea.split(",", -1);
                if (c.length < 4) continue;
                if (codigo.equals(c[0].trim())) return Optional.of(new DistritoElectoral(c[0].trim(), c[1].trim(), c[2].trim(), c[3].trim()));
            }
        }
        return Optional.empty();
    }
}
