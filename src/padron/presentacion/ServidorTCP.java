package padron.presentacion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import padron.servicio.ResultadoConsulta;
import padron.servicio.ServicioPadron;
import padron.util.JsonUtil;
/** Atiende GET|cedula. No conoce ni accede a los archivos de datos. */
public final class ServidorTCP implements Runnable, AutoCloseable {
    private final int puerto; private final ServicioPadron servicio; private final ExecutorService trabajadores; private volatile boolean activo = true; private ServerSocket socketServidor;
    public ServidorTCP(int puerto, ServicioPadron servicio, ExecutorService trabajadores) { this.puerto = puerto; this.servicio = servicio; this.trabajadores = trabajadores; }
    @Override public void run() {
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            socketServidor = servidor;
            while (activo) try { Socket cliente = servidor.accept(); trabajadores.submit(() -> atender(cliente)); } catch (IOException e) { if (activo) System.err.println("Error aceptando TCP: " + e.getMessage()); }
        } catch (IOException e) { throw new IllegalStateException("No se pudo iniciar TCP en puerto " + puerto, e); }
    }
    private void atender(Socket cliente) {
        try (cliente; BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8)); PrintWriter out = new PrintWriter(new OutputStreamWriter(cliente.getOutputStream(), StandardCharsets.UTF_8), true)) {
            // Evita que un cliente que nunca termina su linea ocupe un hilo indefinidamente.
            cliente.setSoTimeout(10_000);
            ResultadoConsulta r;
            try {
                r = interpretar(in.readLine());
            } catch (SocketTimeoutException e) {
                r = servicio.error(408, "Tiempo de espera agotado para la solicitud TCP.");
            }
            out.println(JsonUtil.serializar(r.cuerpo()));
        } catch (IOException e) { System.err.println("Error atendiendo cliente TCP: " + e.getMessage()); }
    }
    private ResultadoConsulta interpretar(String solicitud) {
        if (solicitud == null || solicitud.isBlank()) return servicio.error(400, "Solicitud TCP vacia o incompleta.");
        String[] partes = solicitud.split("\\|", -1);
        if (partes.length != 2) return servicio.error(400, "Solicitud TCP incompleta o mal formada. Use GET|cedula.");
        if (!"GET".equals(partes[0])) return servicio.error(400, "Comando TCP desconocido. Use GET.");
        return servicio.consultar(partes[1].trim());
    }
    @Override public void close() throws IOException { activo = false; if (socketServidor != null) socketServidor.close(); }
}
