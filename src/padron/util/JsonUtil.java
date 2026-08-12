package padron.util;
import padron.dto.ErrorDTO;
import padron.dto.PersonaDTO;
/** Serializador JSON pequeno y sin dependencias externas para los DTO inmutables del proyecto. */
public final class JsonUtil {
    private JsonUtil() { }
    public static String serializar(Object valor) {
        if (valor instanceof PersonaDTO p) return "{\"cedula\":\""+e(p.cedula())+"\",\"nombre\":\""+e(p.nombre())+"\",\"primerApellido\":\""+e(p.primerApellido())+"\",\"segundoApellido\":\""+e(p.segundoApellido())+"\",\"codigoElectoral\":\""+e(p.codigoElectoral())+"\",\"provincia\":\""+e(p.provincia())+"\",\"canton\":\""+e(p.canton())+"\",\"distrito\":\""+e(p.distrito())+"\"}";
        if (valor instanceof ErrorDTO x) return "{\"error\":"+x.error()+",\"codigo\":"+x.codigo()+",\"mensaje\":\""+e(x.mensaje())+"\"}";
        throw new IllegalArgumentException("DTO no soportado");
    }
    private static String e(String s) {
        if (s == null) return "";
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char caracter = s.charAt(i);
            switch (caracter) {
                case '\\' -> resultado.append("\\\\");
                case '"' -> resultado.append("\\\"");
                case '\b' -> resultado.append("\\b");
                case '\f' -> resultado.append("\\f");
                case '\n' -> resultado.append("\\n");
                case '\r' -> resultado.append("\\r");
                case '\t' -> resultado.append("\\t");
                default -> {
                    if (caracter < 0x20) resultado.append(String.format("\\u%04x", (int) caracter));
                    else resultado.append(caracter);
                }
            }
        }
        return resultado.toString();
    }
}
