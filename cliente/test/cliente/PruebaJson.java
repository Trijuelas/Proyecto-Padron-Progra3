package cliente;

import cliente.util.Json;
import java.util.List;
import java.util.Map;

/** Pruebas ejecutables sin librerias externas del analizador JSON. */
public final class PruebaJson {
    public static void main(String[] args) {
        Map<String, Object> persona = Json.analizarObjeto(
                "{\"cedula\":\"115550555\",\"nombre\":\"JUAN\",\"activo\":true,\"edad\":30,\"nulo\":null}");
        verificar("115550555".equals(persona.get("cedula")), "Cadena simple");
        verificar("JUAN".equals(persona.get("nombre")), "Segunda cadena");
        verificar(Boolean.TRUE.equals(persona.get("activo")), "Booleano");
        verificar(persona.get("edad") instanceof Long numero && numero == 30L, "Numero entero");
        verificar(persona.containsKey("nulo") && persona.get("nulo") == null, "Valor nulo");

        Map<String, Object> conEscapes = Json.analizarObjeto("{\"texto\":\"A\\\"B\\\\C\\nD\\tE\"}");
        verificar("A\"B\\C\nD\tE".equals(conEscapes.get("texto")), "Escapado de caracteres especiales");

        Object arreglo = Json.analizar("[1, 2, 3]");
        verificar(arreglo instanceof List<?> lista && lista.size() == 3, "Arreglo JSON");
        verificar(Json.analizar("1e2") instanceof Double numero && numero == 100.0,
                "Numero JSON con exponente");

        verificarInvalido("{cedula: 123}", "Clave sin comillas");
        verificarInvalido("01", "Cero inicial");
        verificarInvalido("1.", "Decimal incompleto");
        verificarInvalido("\"linea\nreal\"", "Control sin escapar");

        System.out.println("Pruebas de Json aprobadas.");
    }

    private static void verificarInvalido(String texto, String caso) {
        try {
            Json.analizar(texto);
            throw new AssertionError("Fallo: JSON invalido aceptado: " + caso);
        } catch (Json.JsonException esperado) {
            // JSON invalido detectado correctamente.
        }
    }

    private static void verificar(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError("Fallo: " + caso);
        }
    }
}
