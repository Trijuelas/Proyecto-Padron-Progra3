package padron.presentacion;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import padron.servicio.ResultadoConsulta;
import padron.servicio.ServicioPadron;
import padron.util.JsonUtil;
/** Expone GET /padron/{cedula}; delega toda consulta al servicio. */
public final class ServidorHTTP implements AutoCloseable {
    private final HttpServer servidor; private final ServicioPadron servicio;
    public ServidorHTTP(int puerto, ServicioPadron servicio, ExecutorService trabajadores) throws IOException { this.servidor = HttpServer.create(new InetSocketAddress(puerto), 0); this.servicio = servicio; servidor.createContext("/", this::atender); servidor.setExecutor(trabajadores); }
    public void iniciar() { servidor.start(); }
    private void atender(HttpExchange e) throws IOException {
        ResultadoConsulta r;
        if (!"GET".equals(e.getRequestMethod())) {
            e.getResponseHeaders().set("Allow", "GET");
            r = servicio.error(405, "Metodo HTTP no permitido.");
        }
        else { String[] p = e.getRequestURI().getPath().split("/", -1); r = p.length == 3 && "padron".equals(p[1]) ? servicio.consultar(p[2]) : servicio.error(404, "Ruta HTTP inexistente. Use /padron/{cedula}."); }
        byte[] cuerpo = JsonUtil.serializar(r.cuerpo()).getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8"); e.sendResponseHeaders(r.codigo(), cuerpo.length); e.getResponseBody().write(cuerpo); e.close();
    }
    @Override public void close() { servidor.stop(0); }
}
