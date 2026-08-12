package padron.util;
import padron.dto.ErrorDTO;
import padron.dto.PersonaDTO;
/** Serializador JSON pequeno y sin dependencias externas para los DTO inmutables del proyecto. */
public final class JsonUtil {
    private JsonUtil() { }
    public static String serializar(Object valor) {
        if (valor instanceof PersonaDTO persona) return serializarPersona(persona);
        if (valor instanceof ErrorDTO error) return serializarError(error);
        throw new IllegalArgumentException("DTO no soportado");
    }

    private static String serializarPersona(PersonaDTO persona) {
        StringBuilder json = new StringBuilder("{");
        agregarCampoTexto(json, "cedula", persona.cedula());
        agregarCampoTexto(json, "nombre", persona.nombre());
        agregarCampoTexto(json, "primerApellido", persona.primerApellido());
        agregarCampoTexto(json, "segundoApellido", persona.segundoApellido());
        agregarCampoTexto(json, "codigoElectoral", persona.codigoElectoral());
        agregarCampoTexto(json, "provincia", persona.provincia());
        agregarCampoTexto(json, "canton", persona.canton());
        agregarCampoTexto(json, "distrito", persona.distrito());
        return json.append('}').toString();
    }

    private static String serializarError(ErrorDTO error) {
        StringBuilder json = new StringBuilder("{");
        agregarCampoBooleano(json, "error", error.error());
        agregarCampoNumero(json, "codigo", error.codigo());
        agregarCampoTexto(json, "mensaje", error.mensaje());
        return json.append('}').toString();
    }

    private static void agregarCampoTexto(StringBuilder json, String nombre, String valor) {
        separarCampo(json);
        agregarCadena(json, nombre);
        json.append(':');
        agregarCadena(json, valor);
    }

    private static void agregarCampoBooleano(StringBuilder json, String nombre, boolean valor) {
        separarCampo(json);
        agregarCadena(json, nombre);
        json.append(':').append(valor);
    }

    private static void agregarCampoNumero(StringBuilder json, String nombre, int valor) {
        separarCampo(json);
        agregarCadena(json, nombre);
        json.append(':').append(valor);
    }

    private static void separarCampo(StringBuilder json) {
        if (json.length() > 1) json.append(',');
    }

    private static void agregarCadena(StringBuilder json, String valor) {
        json.append('"');
        if (valor != null) {
            for (int i = 0; i < valor.length(); i++) {
                char caracter = valor.charAt(i);
                switch (caracter) {
                    case '\\' -> json.append("\\\\");
                    case '"' -> json.append("\\\"");
                    case '\b' -> json.append("\\b");
                    case '\f' -> json.append("\\f");
                    case '\n' -> json.append("\\n");
                    case '\r' -> json.append("\\r");
                    case '\t' -> json.append("\\t");
                    default -> {
                        if (caracter < 0x20) agregarUnicode(json, caracter);
                        else json.append(caracter);
                    }
                }
            }
        }
        json.append('"');
    }

    private static void agregarUnicode(StringBuilder json, char caracter) {
        json.append("\\u");
        String hexadecimal = Integer.toHexString(caracter);
        for (int i = hexadecimal.length(); i < 4; i++) json.append('0');
        json.append(hexadecimal);
    }
}
