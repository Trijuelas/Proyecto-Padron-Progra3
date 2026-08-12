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
        ServidorTCP tcp = new ServidorTCP(c.puertoTcp(), servicio, trabajadores);
        ServidorHTTP http = new ServidorHTTP(c.puertoHttp(), servicio, trabajadores);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { try { tcp.close(); } catch (Exception e) { System.err.println("Error cerrando TCP: " + e.getMessage()); } http.close(); trabajadores.shutdown(); }));
        new Thread(tcp, "servidor-tcp").start(); http.iniciar();
        System.out.printf("Servidor listo: TCP %d, HTTP %d%n", c.puertoTcp(), c.puertoHttp());
    }
}
