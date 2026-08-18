package cliente.comunicacion;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP: realiza "GET /padron/{cedula}" y devuelve el cuerpo JSON
 * crudo recibido (exitoso o de error). No conoce DTO ni JSON; esa
 * interpretacion es responsabilidad de la capa de logica (ServicioConsulta).
 */
public final class ClienteHTTP {
    private final String servidor;
    private final int puerto;
    private final int tiempoEsperaMs;

    public ClienteHTTP(String servidor, int puerto, int tiempoEsperaMs) {
        this.servidor = servidor;
        this.puerto = puerto;
        this.tiempoEsperaMs = tiempoEsperaMs;
    }

    public String consultar(String cedula) throws IOException {
        URL url = construirUrl(cedula);
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
        conexion.setRequestMethod("GET");
        conexion.setConnectTimeout(tiempoEsperaMs);
        conexion.setReadTimeout(tiempoEsperaMs);
        try {
            int codigo = conexion.getResponseCode();
            InputStream flujo = codigo < 400 ? conexion.getInputStream() : conexion.getErrorStream();
            if (flujo == null) {
                throw new IOException("El servidor respondio " + codigo + " sin cuerpo.");
            }
            return leerCompleto(flujo);
        } finally {
            conexion.disconnect();
        }
    }

    private URL construirUrl(String cedula) throws IOException {
        try {
            return new URI("http", null, servidor, puerto, "/padron/" + cedula, null, null).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("No fue posible construir la URL de consulta.", e);
        }
    }

    private static String leerCompleto(InputStream flujo) throws IOException {
        return new String(flujo.readAllBytes(), StandardCharsets.UTF_8);
    }
}
