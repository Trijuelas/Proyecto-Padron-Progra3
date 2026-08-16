package cliente;

import cliente.config.Configuracion;
import cliente.presentacion.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada de la aplicacion cliente del Padron Electoral. */
public final class App {
    public static void main(String[] args) throws Exception {
        Configuracion configuracion = Configuracion.cargar(args);
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorado) {
                // Se conserva el look and feel por defecto si el del sistema no esta disponible.
            }
            new VentanaPrincipal(configuracion).setVisible(true);
        });
    }
}
