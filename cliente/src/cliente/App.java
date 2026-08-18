package cliente;

import cliente.config.Configuracion;
import cliente.presentacion.Tema;
import cliente.presentacion.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada de la aplicacion cliente del Padron Electoral. */
public final class App {
    public static void main(String[] args) throws Exception {
        Configuracion configuracion = Configuracion.cargar(args);
        SwingUtilities.invokeLater(() -> {
            try {
                // Se usa el look and feel multiplataforma (Metal) como base, en vez del
                // nativo del sistema operativo: los componentes propios de la interfaz
                // (botones, tarjetas, banner de estado) se pintan a mano y necesitan una
                // base predecible para verse consistentes en cualquier sistema operativo.
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignorado) {
                // Se conserva el look and feel por defecto si el multiplataforma no esta disponible.
            }
            aplicarEstilosBase();
            new VentanaPrincipal(configuracion).setVisible(true);
        });
    }

    /** Ajustes minimos de UIManager para que los controles nativos (campo de texto,
     * combo, barra de progreso) combinen con la paleta definida en Tema. */
    private static void aplicarEstilosBase() {
        UIManager.put("TextField.font", Tema.fuenteCampo());
        UIManager.put("ComboBox.font", Tema.fuenteCampo());
        UIManager.put("Label.font", Tema.fuenteValor());
        UIManager.put("ProgressBar.foreground", Tema.PRIMARIO);
        UIManager.put("ScrollBar.thumb", Tema.BORDE);
    }
}
