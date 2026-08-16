package cliente;

import cliente.comunicacion.ClienteHTTP;
import cliente.comunicacion.ClienteTCP;
import cliente.dto.PersonaDTO;
import cliente.servicio.Protocolo;
import cliente.servicio.ResultadoConsulta;
import cliente.servicio.ServicioConsulta;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pruebas de ServicioConsulta contra servidores de prueba TCP y HTTP
 * livianos, sin depender del proyecto del servidor (cliente y servidor son
 * programas independientes).
 */
public final class PruebaServicioConsulta {
    public static void main(String[] args) throws Exception {
        probarConsultaExitosaTcp();
        probarConsultaExitosaHttp();
        probarCedulaInexistente();
        probarValidacionLocal();
        probarServidorNoDisponible();
        probarJsonInvalido();
        System.out.println("Pruebas de ServicioConsulta aprobadas.");
    }

    private static void probarConsultaExitosaTcp() throws Exception {
        try (ServidorTcpDePrueba servidor = new ServidorTcpDePrueba(
                "{\"cedula\":\"115550555\",\"nombre\":\"JUAN\",\"primerApellido\":\"PEREZ\","
                        + "\"segundoApellido\":\"RODRIGUEZ\",\"codigoElectoral\":\"101001\","
                        + "\"provincia\":\"SAN JOSE\",\"canton\":\"CENTRAL\",\"distrito\":\"CARMEN\"}")) {
            ServicioConsulta servicio = new ServicioConsulta(
                    new ClienteTCP("127.0.0.1", servidor.puerto(), 2000),
                    new ClienteHTTP("127.0.0.1", 1, 2000));
            ResultadoConsulta resultado = servicio.consultar("115550555", Protocolo.TCP);
            verificar(resultado.exito(), "Consulta TCP exitosa");
            PersonaDTO persona = resultado.persona();
            verificar("JUAN".equals(persona.nombre()) && "SAN JOSE".equals(persona.provincia()),
                    "Campos deserializados via TCP");
        }
    }

    private static void probarConsultaExitosaHttp() throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/padron/115550555", exchange -> {
            byte[] cuerpo = ("{\"cedula\":\"115550555\",\"nombre\":\"JUAN\",\"primerApellido\":\"PEREZ\","
                    + "\"segundoApellido\":\"RODRIGUEZ\",\"codigoElectoral\":\"101001\","
                    + "\"provincia\":\"SAN JOSE\",\"canton\":\"CENTRAL\",\"distrito\":\"CARMEN\"}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, cuerpo.length);
            try (OutputStream salida = exchange.getResponseBody()) {
                salida.write(cuerpo);
            }
        });
        ExecutorService ejecutor = Executors.newSingleThreadExecutor(PruebaServicioConsulta::hiloDemonio);
        servidor.setExecutor(ejecutor);
        servidor.start();
        try {
            ServicioConsulta servicio = new ServicioConsulta(
                    new ClienteTCP("127.0.0.1", 1, 2000),
                    new ClienteHTTP("127.0.0.1", servidor.getAddress().getPort(), 2000));
            ResultadoConsulta resultado = servicio.consultar("115550555", Protocolo.HTTP);
            verificar(resultado.exito(), "Consulta HTTP exitosa");
            verificar("CARMEN".equals(resultado.persona().distrito()), "Campos deserializados via HTTP");
        } finally {
            servidor.stop(0);
            ejecutor.shutdownNow();
        }
    }

    private static Thread hiloDemonio(Runnable tarea) {
        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        return hilo;
    }

    private static void probarCedulaInexistente() throws Exception {
        try (ServidorTcpDePrueba servidor = new ServidorTcpDePrueba(
                "{\"error\":true,\"codigo\":404,\"mensaje\":\"No se encontro una persona con la cedula indicada.\"}")) {
            ServicioConsulta servicio = new ServicioConsulta(
                    new ClienteTCP("127.0.0.1", servidor.puerto(), 2000),
                    new ClienteHTTP("127.0.0.1", 1, 2000));
            ResultadoConsulta resultado = servicio.consultar("000000000", Protocolo.TCP);
            verificar(!resultado.exito() && resultado.error().codigo() == 404, "Cedula inexistente");
        }
    }

    private static void probarValidacionLocal() {
        ServicioConsulta servicio = new ServicioConsulta(
                new ClienteTCP("127.0.0.1", 1, 500), new ClienteHTTP("127.0.0.1", 1, 500));
        verificar(!servicio.consultar("", Protocolo.TCP).exito(), "Cedula vacia no se envia al servidor");
        verificar(!servicio.consultar("abc", Protocolo.TCP).exito(), "Cedula con formato invalido");
    }

    private static void probarServidorNoDisponible() throws Exception {
        int puertoLibre = puertoDisponible();
        ServicioConsulta servicio = new ServicioConsulta(
                new ClienteTCP("127.0.0.1", puertoLibre, 800),
                new ClienteHTTP("127.0.0.1", puertoLibre, 800));
        ResultadoConsulta resultado = servicio.consultar("115550555", Protocolo.TCP);
        verificar(!resultado.exito(), "Servidor no disponible se maneja sin lanzar excepcion");
    }

    private static void probarJsonInvalido() throws Exception {
        try (ServidorTcpDePrueba servidor = new ServidorTcpDePrueba("esto no es JSON")) {
            ServicioConsulta servicio = new ServicioConsulta(
                    new ClienteTCP("127.0.0.1", servidor.puerto(), 2000),
                    new ClienteHTTP("127.0.0.1", 1, 2000));
            ResultadoConsulta resultado = servicio.consultar("115550555", Protocolo.TCP);
            verificar(!resultado.exito(), "JSON invalido se maneja como error controlado");
        }
    }

    private static int puertoDisponible() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void verificar(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError("Fallo: " + caso);
        }
    }

    /** Servidor TCP minimo de prueba: responde siempre la misma linea configurada. */
    private static final class ServidorTcpDePrueba implements AutoCloseable {
        private final ServerSocket socketServidor;
        private final Thread hilo;
        private volatile boolean activo = true;

        ServidorTcpDePrueba(String respuesta) throws IOException {
            socketServidor = new ServerSocket(0);
            hilo = new Thread(() -> atender(respuesta));
            hilo.start();
        }

        int puerto() {
            return socketServidor.getLocalPort();
        }

        private void atender(String respuesta) {
            while (activo) {
                try (Socket cliente = socketServidor.accept();
                     BufferedReader entrada = new BufferedReader(
                             new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true, StandardCharsets.UTF_8)) {
                    entrada.readLine();
                    salida.println(respuesta);
                } catch (IOException e) {
                    if (activo) {
                        break;
                    }
                }
            }
        }

        @Override
        public void close() throws IOException {
            activo = false;
            socketServidor.close();
            try {
                hilo.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
