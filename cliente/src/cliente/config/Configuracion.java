package cliente.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Centraliza la direccion del servidor y los puertos. Nunca se asume
 * "localhost" de forma obligatoria: la direccion es configurable para poder
 * ejecutar el cliente contra un servidor remoto.
 */
public final class Configuracion {
    private final String servidor;
    private final int puertoTcp;
    private final int puertoHttp;
    private final int tiempoEsperaMs;

    private Configuracion(Properties p) {
        servidor = p.getProperty("servidor.host", "localhost");
        puertoTcp = entero(p, "tcp.port", 5000);
        puertoHttp = entero(p, "http.port", 8080);
        tiempoEsperaMs = entero(p, "timeout.ms", 8000);
    }

    public static Configuracion cargar(String[] args) throws IOException {
        Properties p = new Properties();
        Path archivo = Path.of(System.getProperty("config", "config.properties"));
        if (Files.exists(archivo)) {
            try (InputStream in = Files.newInputStream(archivo)) {
                p.load(in);
            }
        }
        for (String arg : args) {
            int i = arg.indexOf('=');
            if (arg.startsWith("--") && i > 2) {
                p.setProperty(arg.substring(2, i), arg.substring(i + 1));
            }
        }
        copiarSistema(p, "servidor.host");
        copiarSistema(p, "tcp.port");
        copiarSistema(p, "http.port");
        copiarSistema(p, "timeout.ms");
        return new Configuracion(p);
    }

    private static void copiarSistema(Properties p, String clave) {
        String v = System.getProperty(clave);
        if (v != null) {
            p.setProperty(clave, v);
        }
    }

    private static int entero(Properties p, String clave, int defecto) {
        try {
            int v = Integer.parseInt(p.getProperty(clave, String.valueOf(defecto)));
            if (v < 1 || v > 65535) {
                throw new NumberFormatException();
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor invalido para " + clave, e);
        }
    }

    public String servidor() { return servidor; }
    public int puertoTcp() { return puertoTcp; }
    public int puertoHttp() { return puertoHttp; }
    public int tiempoEsperaMs() { return tiempoEsperaMs; }
}
