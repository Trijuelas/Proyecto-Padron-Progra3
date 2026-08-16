package cliente.servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Guarda en memoria las consultas realizadas durante la sesion actual de la
 * aplicacion (no se escribe en ningun archivo ni base de datos: se pierde al
 * cerrar el programa, por decision explicita del proyecto). Mantiene como
 * maximo {@value #MAX_ENTRADAS} entradas, descartando las mas antiguas.
 *
 * Todos los metodos se invocan siempre desde el hilo de eventos de Swing
 * (creacion de VentanaPrincipal y callbacks de SwingWorker.done()), por lo
 * que no necesita sincronizacion adicional.
 */
public final class HistorialConsultas {
    private static final int MAX_ENTRADAS = 50;
    private final LinkedList<EntradaHistorial> entradas = new LinkedList<>();

    public void registrar(EntradaHistorial entrada) {
        entradas.addFirst(entrada);
        while (entradas.size() > MAX_ENTRADAS) {
            entradas.removeLast();
        }
    }

    /** Copia de solo lectura, de la mas reciente a la mas antigua. */
    public List<EntradaHistorial> obtenerTodo() {
        return Collections.unmodifiableList(new ArrayList<>(entradas));
    }

    public void limpiar() {
        entradas.clear();
    }
}
