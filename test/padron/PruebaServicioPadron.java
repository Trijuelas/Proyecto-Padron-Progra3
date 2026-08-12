package padron;
import java.nio.file.Files;
import java.nio.file.Path;
import padron.repositorio.RepositorioDistritos;
import padron.repositorio.RepositorioPadron;
import padron.servicio.ServicioPadron;
/** Prueba ejecutable sin librerias externas: datos, inexistente y validacion. */
public final class PruebaServicioPadron {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("padron-prueba-");
        Path padron = dir.resolve("padron.txt"), distritos = dir.resolve("distelec.txt");
        Files.writeString(padron, "linea,incompleta\n115550555,101001,RELLENO,20280101,00001,JUAN,PEREZ,RODRIGUEZ\n");
        Files.writeString(distritos, "101001,SAN JOSE,CENTRAL,CARMEN\n");
        ServicioPadron s = new ServicioPadron(new RepositorioPadron(padron), new RepositorioDistritos(distritos));
        verificar(s.consultar("115550555").codigo() == 200, "Consulta existente");
        verificar(s.consultar("000000000").codigo() == 404, "Consulta inexistente");
        verificar(s.consultar("abc").codigo() == 400, "Cedula invalida");
        verificar(s.consultar("115550555").cuerpo().toString().contains("JUAN"), "Campos del padron");
        ServicioPadron archivoInexistente = new ServicioPadron(new RepositorioPadron(dir.resolve("no-existe.txt")), new RepositorioDistritos(distritos));
        verificar(archivoInexistente.consultar("115550555").codigo() == 500, "Archivo inexistente");
        System.out.println("Pruebas de ServicioPadron aprobadas.");
    }
    private static void verificar(boolean condicion, String caso) { if (!condicion) throw new AssertionError("Fallo: " + caso); }
}
