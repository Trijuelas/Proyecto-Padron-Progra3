package cliente.comunicacion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Cliente TCP: envia "GET|cedula" y devuelve la linea JSON cruda recibida.
 * No conoce DTO ni JSON; esa interpretacion es responsabilidad de la capa
 * de logica (ServicioConsulta).
 */
public final class ClienteTCP {
    private final String servidor;
    private final int puerto;
    private final int tiempoEsperaMs;

    public ClienteTCP(String servidor, int puerto, int tiempoEsperaMs) {
        this.servidor = servidor;
        this.puerto = puerto;
        this.tiempoEsperaMs = tiempoEsperaMs;
    }

    public String consultar(String cedula) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(servidor, puerto), tiempoEsperaMs);
            socket.setSoTimeout(tiempoEsperaMs);
            try (PrintWriter salida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                 BufferedReader entrada = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                salida.println("GET|" + cedula);
                String respuesta = entrada.readLine();
                if (respuesta == null) {
                    throw new IOException("El servidor cerro la conexion sin responder.");
                }
                return respuesta;
            }
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException("Tiempo de espera agotado consultando el servidor por TCP.");
        }
    }
}
