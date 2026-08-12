package padron.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Centraliza la configuracion modificable del servidor. */
public final class Configuracion {
    private final int puertoTcp;
    private final int puertoHttp;
    private final int hilos;
    private final Path archivoPadron;
    private final Path archivoDistritos;

    private Configuracion(Properties p) {
        puertoTcp = entero(p, "tcp.port", 5000);
        puertoHttp = entero(p, "http.port", 8080);
        hilos = entero(p, "worker.threads", 12);
        archivoPadron = Path.of(requerido(p, "padron.path"));
        archivoDistritos = Path.of(requerido(p, "distritos.path"));
    }

    public static Configuracion cargar(String[] args) throws IOException {
        Properties p = new Properties();
        Path archivo = Path.of(System.getProperty("config", "config.properties"));
        if (Files.exists(archivo)) try (InputStream in = Files.newInputStream(archivo)) { p.load(in); }
        for (String arg : args) {
            int i = arg.indexOf('=');
            if (arg.startsWith("--") && i > 2) p.setProperty(arg.substring(2, i), arg.substring(i + 1));
        }
        copiarSistema(p, "tcp.port"); copiarSistema(p, "http.port"); copiarSistema(p, "worker.threads");
        copiarSistema(p, "padron.path"); copiarSistema(p, "distritos.path");
        return new Configuracion(p);
    }
    private static void copiarSistema(Properties p, String clave) { String v = System.getProperty(clave); if (v != null) p.setProperty(clave, v); }
    private static String requerido(Properties p, String clave) { String v = p.getProperty(clave); if (v == null || v.isBlank()) throw new IllegalArgumentException("Falta configurar " + clave); return v; }
    private static int entero(Properties p, String clave, int defecto) { try { int v = Integer.parseInt(p.getProperty(clave, String.valueOf(defecto))); if (v < 1 || v > 65535) throw new NumberFormatException(); return v; } catch (NumberFormatException e) { throw new IllegalArgumentException("Valor invalido para " + clave, e); } }
    public int puertoTcp() { return puertoTcp; } public int puertoHttp() { return puertoHttp; } public int hilos() { return hilos; }
    public Path archivoPadron() { return archivoPadron; } public Path archivoDistritos() { return archivoDistritos; }
}
