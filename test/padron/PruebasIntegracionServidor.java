package padron;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import padron.presentacion.ServidorHTTP;
import padron.presentacion.ServidorTCP;
import padron.repositorio.RepositorioDistritos;
import padron.repositorio.RepositorioPadron;
import padron.servicio.ServicioPadron;

/** Pruebas reales de protocolos y concurrencia, sin dependencias externas. */
public final class PruebasIntegracionServidor {
    private PruebasIntegracionServidor() { }

    public static void main(String[] args) throws Exception {
        Path directorio = Files.createTempDirectory("padron-integracion-");
        Path archivoPadron = directorio.resolve("PADRON.txt");
        Path archivoDistritos = directorio.resolve("distelec.txt");
        Files.writeString(archivoPadron, "115550555,101001,R,20280101,00001,JUAN,PEREZ,RODRIGUEZ\n");
        Files.writeString(archivoDistritos, "101001,SAN JOSE,CENTRAL,CARMEN\n");
        ServicioPadron servicio = new ServicioPadron(new RepositorioPadron(archivoPadron),
                new RepositorioDistritos(archivoDistritos));
        int puertoTcp = puertoDisponible();
        int puertoHttp = puertoDisponible();
        ExecutorService trabajadores = Executors.newFixedThreadPool(6);
        ServidorTCP tcp = new ServidorTCP(puertoTcp, servicio, trabajadores);
        ServidorHTTP http = new ServidorHTTP(puertoHttp, servicio, trabajadores);
        Thread hiloTcp = new Thread(tcp, "prueba-servidor-tcp");
        hiloTcp.start();
        http.iniciar();
        try {
            esperarTcp(puertoTcp);
            probarTcp(puertoTcp);
            probarHttp(puertoHttp);
            probarConcurrencia(puertoTcp, puertoHttp);
            System.out.println("Pruebas de integracion TCP, HTTP y concurrencia aprobadas.");
        } finally {
            tcp.close();
            http.close();
            trabajadores.shutdownNow();
            hiloTcp.join(2_000);
        }
    }

    private static void probarTcp(int puerto) throws Exception {
        verificar(tcp(puerto, "GET|115550555").contains("\"provincia\":\"SAN JOSE\""), "TCP consulta correcta y territorial");
        verificar(tcp(puerto, "GET|000000000").contains("\"codigo\":404"), "TCP cedula inexistente");
        verificar(tcp(puerto, "POST|115550555").contains("\"codigo\":400"), "TCP comando desconocido");
        verificar(tcp(puerto, "GET").contains("\"codigo\":400"), "TCP solicitud incompleta");
        verificar(tcp(puerto, "GET|abc").contains("\"codigo\":400"), "TCP cedula invalida");
        verificar(tcp(puerto, "GET|115550555").contains("\"cedula\":\"115550555\""), "TCP continua tras errores");
    }

    private static void probarHttp(int puerto) throws Exception {
        RespuestaHttp correcta = http(puerto, "GET", "/padron/115550555");
        verificar(correcta.codigo == 200 && correcta.cuerpo.contains("\"distrito\":\"CARMEN\""), "HTTP consulta correcta");
        verificar(correcta.tipoContenido.startsWith("application/json"), "HTTP Content-Type JSON");
        verificar(http(puerto, "GET", "/padron/000000000").codigo == 404, "HTTP cedula inexistente");
        verificar(http(puerto, "GET", "/otra").codigo == 404, "HTTP ruta inexistente");
        RespuestaHttp metodo = http(puerto, "POST", "/padron/115550555");
        verificar(metodo.codigo == 405 && "GET".equals(metodo.allow), "HTTP metodo no permitido");
        verificar(http(puerto, "GET", "/padron/").codigo == 400, "HTTP ruta mal formada");
    }

    private static void probarConcurrencia(int puertoTcp, int puertoHttp) throws Exception {
        ExecutorService clientes = Executors.newFixedThreadPool(12);
        try {
            List<Callable<Boolean>> tareas = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                final int indice = i;
                tareas.add(() -> indice % 2 == 0
                        ? tcp(puertoTcp, "GET|115550555").contains("\"cedula\"")
                        : http(puertoHttp, "GET", "/padron/115550555").codigo == 200);
            }
            for (Future<Boolean> resultado : clientes.invokeAll(tareas)) {
                verificar(resultado.get(), "Cliente concurrente atendido");
            }
        } finally {
            clientes.shutdownNow();
        }
    }

    private static String tcp(int puerto, String solicitud) throws Exception {
        try (Socket cliente = new Socket("127.0.0.1", puerto);
             PrintWriter salida = new PrintWriter(new OutputStreamWriter(cliente.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8))) {
            salida.println(solicitud);
            return entrada.readLine();
        }
    }

    private static RespuestaHttp http(int puerto, String metodo, String ruta) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) new URL("http://127.0.0.1:" + puerto + ruta).openConnection();
        conexion.setRequestMethod(metodo);
        int codigo = conexion.getResponseCode();
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(
                codigo < 400 ? conexion.getInputStream() : conexion.getErrorStream(), StandardCharsets.UTF_8))) {
            return new RespuestaHttp(codigo, entrada.readLine(), conexion.getHeaderField("Content-Type"), conexion.getHeaderField("Allow"));
        }
    }

    private static void esperarTcp(int puerto) throws Exception {
        for (int intento = 0; intento < 20; intento++) {
            try (Socket ignorado = new Socket("127.0.0.1", puerto)) { return; }
            catch (Exception e) { Thread.sleep(50); }
        }
        throw new AssertionError("El servidor TCP no inicio a tiempo");
    }

    private static int puertoDisponible() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }
    private static void verificar(boolean condicion, String caso) {
        if (!condicion) throw new AssertionError("Fallo: " + caso);
    }
    private record RespuestaHttp(int codigo, String cuerpo, String tipoContenido, String allow) { }
}
