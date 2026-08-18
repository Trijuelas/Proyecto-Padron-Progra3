package padron;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import padron.config.Configuracion;
import padron.presentacion.ServidorHTTP;
import padron.presentacion.ServidorTCP;
import padron.repositorio.RepositorioDistritos;
import padron.repositorio.RepositorioPadron;
import padron.servicio.ServicioPadron;
/** Punto de entrada del servidor de consultas del Padron Electoral. */
public final class App {
    public static void main(String[] args) throws Exception {
        Configuracion c = Configuracion.cargar(args);
        ServicioPadron servicio = new ServicioPadron(new RepositorioPadron(c.archivoPadron()), new RepositorioDistritos(c.archivoDistritos()));
        ExecutorService trabajadores = Executors.newFixedThreadPool(c.hilos());
        ServidorTCP tcp = null;
        ServidorHTTP http;
        try {
            tcp = new ServidorTCP(c.puertoTcp(), servicio, trabajadores);
            http = new ServidorHTTP(c.puertoHttp(), servicio, trabajadores);
        } catch (Exception e) {
            if (tcp != null) {
                try {
                    tcp.close();
                } catch (Exception cierre) {
                    e.addSuppressed(cierre);
                }
            }
            trabajadores.shutdownNow();
            throw e;
        }
        ServidorTCP servidorTcp = tcp;
        ServidorHTTP servidorHttp = http;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { try { servidorTcp.close(); } catch (Exception e) { System.err.println("Error cerrando TCP: " + e.getMessage()); } servidorHttp.close(); trabajadores.shutdown(); }));
        new Thread(servidorTcp, "servidor-tcp").start(); servidorHttp.iniciar();
        System.out.printf("Servidor listo: TCP %d, HTTP %d%n", c.puertoTcp(), c.puertoHttp());
    }
}
