package cliente.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Analizador JSON minimo, sin dependencias externas, usado para deserializar
 * las respuestas del servidor. Recorre el texto caracter por caracter; no se
 * usa split/substring para extraer valores, tal como exige el enunciado.
 */
public final class Json {
    private final String texto;
    private int posicion;

    private Json(String texto) {
        this.texto = texto;
        this.posicion = 0;
    }

    /** Analiza un texto JSON arbitrario y devuelve el valor raiz. */
    public static Object analizar(String texto) {
        if (texto == null) {
            throw new JsonException("Texto JSON vacio.");
        }
        Json analizador = new Json(texto);
        analizador.saltarEspacios();
        Object valor = analizador.leerValor();
        analizador.saltarEspacios();
        if (analizador.posicion != analizador.texto.length()) {
            throw new JsonException("Contenido inesperado despues del JSON.");
        }
        return valor;
    }

    /** Analiza un texto JSON que se espera sea un objeto (como las respuestas del servidor). */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> analizarObjeto(String texto) {
        Object valor = analizar(texto);
        if (!(valor instanceof Map)) {
            throw new JsonException("Se esperaba un objeto JSON.");
        }
        return (Map<String, Object>) valor;
    }

    private Object leerValor() {
        if (posicion >= texto.length()) {
            throw new JsonException("Fin inesperado del JSON.");
        }
        char actual = texto.charAt(posicion);
        return switch (actual) {
            case '{' -> leerObjeto();
            case '[' -> leerArreglo();
            case '"' -> leerCadena();
            case 't', 'f' -> leerBooleano();
            case 'n' -> leerNulo();
            default -> leerNumero();
        };
    }

    private Map<String, Object> leerObjeto() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        esperar('{');
        saltarEspacios();
        if (miraCaracter() == '}') {
            posicion++;
            return resultado;
        }
        while (true) {
            saltarEspacios();
            String clave = leerCadena();
            saltarEspacios();
            esperar(':');
            saltarEspacios();
            resultado.put(clave, leerValor());
            saltarEspacios();
            char siguiente = miraCaracter();
            if (siguiente == ',') {
                posicion++;
                continue;
            }
            if (siguiente == '}') {
                posicion++;
                break;
            }
            throw new JsonException("Se esperaba ',' o '}' en un objeto JSON.");
        }
        return resultado;
    }

    private List<Object> leerArreglo() {
        List<Object> resultado = new ArrayList<>();
        esperar('[');
        saltarEspacios();
        if (miraCaracter() == ']') {
            posicion++;
            return resultado;
        }
        while (true) {
            saltarEspacios();
            resultado.add(leerValor());
            saltarEspacios();
            char siguiente = miraCaracter();
            if (siguiente == ',') {
                posicion++;
                continue;
            }
            if (siguiente == ']') {
                posicion++;
                break;
            }
            throw new JsonException("Se esperaba ',' o ']' en un arreglo JSON.");
        }
        return resultado;
    }

    private String leerCadena() {
        esperar('"');
        StringBuilder resultado = new StringBuilder();
        while (true) {
            if (posicion >= texto.length()) {
                throw new JsonException("Cadena JSON sin cerrar.");
            }
            char actual = texto.charAt(posicion++);
            if (actual == '"') {
                break;
            }
            if (actual == '\\') {
                if (posicion >= texto.length()) {
                    throw new JsonException("Secuencia de escape incompleta.");
                }
                char escape = texto.charAt(posicion++);
                switch (escape) {
                    case '"' -> resultado.append('"');
                    case '\\' -> resultado.append('\\');
                    case '/' -> resultado.append('/');
                    case 'b' -> resultado.append('\b');
                    case 'f' -> resultado.append('\f');
                    case 'n' -> resultado.append('\n');
                    case 'r' -> resultado.append('\r');
                    case 't' -> resultado.append('\t');
                    case 'u' -> {
                        if (posicion + 4 > texto.length()) {
                            throw new JsonException("Secuencia unicode incompleta.");
                        }
                        String hex = texto.substring(posicion, posicion + 4);
                        resultado.append((char) Integer.parseInt(hex, 16));
                        posicion += 4;
                    }
                    default -> throw new JsonException("Secuencia de escape invalida: \\" + escape);
                }
            } else {
                resultado.append(actual);
            }
        }
        return resultado.toString();
    }

    private Boolean leerBooleano() {
        if (texto.startsWith("true", posicion)) {
            posicion += 4;
            return Boolean.TRUE;
        }
        if (texto.startsWith("false", posicion)) {
            posicion += 5;
            return Boolean.FALSE;
        }
        throw new JsonException("Valor booleano invalido.");
    }

    private Object leerNulo() {
        if (texto.startsWith("null", posicion)) {
            posicion += 4;
            return null;
        }
        throw new JsonException("Valor 'null' invalido.");
    }

    private Number leerNumero() {
        int inicio = posicion;
        if (miraCaracter() == '-') {
            posicion++;
        }
        boolean esDecimal = false;
        while (posicion < texto.length()) {
            char actual = texto.charAt(posicion);
            if (Character.isDigit(actual)) {
                posicion++;
            } else if (actual == '.' || actual == 'e' || actual == 'E' || actual == '+' || actual == '-') {
                esDecimal = esDecimal || actual == '.';
                posicion++;
            } else {
                break;
            }
        }
        String numero = texto.substring(inicio, posicion);
        if (numero.isEmpty() || "-".equals(numero)) {
            throw new JsonException("Numero JSON invalido.");
        }
        return esDecimal ? Double.parseDouble(numero) : (Number) Long.parseLong(numero);
    }

    private void saltarEspacios() {
        while (posicion < texto.length() && Character.isWhitespace(texto.charAt(posicion))) {
            posicion++;
        }
    }

    private char miraCaracter() {
        if (posicion >= texto.length()) {
            throw new JsonException("Fin inesperado del JSON.");
        }
        return texto.charAt(posicion);
    }

    private void esperar(char esperado) {
        if (posicion >= texto.length() || texto.charAt(posicion) != esperado) {
            throw new JsonException("Se esperaba '" + esperado + "' en la posicion " + posicion + ".");
        }
        posicion++;
    }

    /** Error al analizar un texto que no es JSON valido. */
    public static final class JsonException extends RuntimeException {
        public JsonException(String mensaje) {
            super(mensaje);
        }
    }
}
