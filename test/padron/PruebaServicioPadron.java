package padron;
import java.nio.file.Files;
import java.nio.file.Path;
import padron.dto.PersonaDTO;
import padron.repositorio.RepositorioDistritos;
import padron.repositorio.RepositorioPadron;
import padron.servicio.ServicioPadron;
import padron.util.JsonUtil;
/** Prueba ejecutable sin librerias externas: datos, inexistente y validacion. */
public final class PruebaServicioPadron {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("padron-prueba-");
        Path padron = dir.resolve("padron.txt"), distritos = dir.resolve("distelec.txt");
        Files.writeString(padron, "linea,incompleta\n115550555,101001,20280101,00001,JUAN,PEREZ,RODRIGUEZ\n");
        Files.writeString(distritos, "101001,SAN JOSE,CENTRAL,CARMEN\n");
        ServicioPadron s = new ServicioPadron(new RepositorioPadron(padron), new RepositorioDistritos(distritos));
        verificar(s.consultar("115550555").codigo() == 200, "Consulta existente");
        verificar(s.consultar("000000000").codigo() == 404, "Consulta inexistente");
        verificar(s.consultar("abc").codigo() == 400, "Cedula invalida");
        PersonaDTO persona = (PersonaDTO) s.consultar("115550555").cuerpo();
        verificar("JUAN".equals(persona.nombre()) && "PEREZ".equals(persona.primerApellido())
                && "RODRIGUEZ".equals(persona.segundoApellido()), "Campos del padron");
        ServicioPadron archivoInexistente = new ServicioPadron(new RepositorioPadron(dir.resolve("no-existe.txt")), new RepositorioDistritos(distritos));
        verificar(archivoInexistente.consultar("115550555").codigo() == 500, "Archivo inexistente");
        String jsonEspecial = JsonUtil.serializar(new PersonaDTO("115550555", "A\"B\\C\nD\tE", "PEREZ", "RODRIGUEZ", "101001", "SAN JOSE", "CENTRAL", "CARMEN"));
        verificar(jsonEspecial.contains("A\\\"B\\\\C\\nD\\tE"), "Escapado JSON de caracteres especiales");

        // distelec.txt real viene en ISO-8859-1 y trae tildes/enies reales (ej. "PEÑAS BLANCAS").
        // Se escriben los bytes explicitamente en ISO-8859-1 para simular el archivo original.
        Path distritosConEnie = dir.resolve("distelec-enie.txt");
        String distritoConEnie = "119034,SAN JOSE,PEREZ ZELEDON,PEÑAS BLANCAS\n";
        Files.write(distritosConEnie, distritoConEnie.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
        Path padronParaEnie = dir.resolve("padron-enie.txt");
        Files.writeString(padronParaEnie, "115550556,119034,20280101,00001,MARIA,SOLIS,VARGAS\n");
        ServicioPadron servicioEnie = new ServicioPadron(new RepositorioPadron(padronParaEnie), new RepositorioDistritos(distritosConEnie));
        var resultadoEnie = servicioEnie.consultar("115550556");
        verificar(resultadoEnie.codigo() == 200, "Consulta con distrito con enie no falla");
        PersonaDTO personaEnie = (PersonaDTO) resultadoEnie.cuerpo();
        verificar("PEÑAS BLANCAS".equals(personaEnie.distrito()), "Distrito con enie leido en ISO-8859-1");

        System.out.println("Pruebas de ServicioPadron aprobadas.");
    }
    private static void verificar(boolean condicion, String caso) { if (!condicion) throw new AssertionError("Fallo: " + caso); }
}
