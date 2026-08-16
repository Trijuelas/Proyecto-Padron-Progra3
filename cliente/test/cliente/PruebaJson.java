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

        try {
            Json.analizar("{cedula: 123}");
            throw new AssertionError("Fallo: se esperaba un error de JSON invalido");
        } catch (Json.JsonException esperado) {
            // JSON invalido detectado correctamente.
        }

        System.out.println("Pruebas de Json aprobadas.");
    }

    private static void verificar(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError("Fallo: " + caso);
        }
    }
}
