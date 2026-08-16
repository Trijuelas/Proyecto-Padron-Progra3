package cliente.presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Banner de retroalimentacion (info / exito / error / cargando) con una
 * franja de acento a la izquierda, reemplazando el JLabel de texto plano
 * que antes era facil de pasar por alto. Reserva su propio espacio siempre
 * visible para que la ventana no salte de tamano al aparecer un mensaje.
 */
public final class PanelEstado extends JPanel {

    /** Tipo de mensaje mostrado, cada uno con su propio color de acento. */
    public enum Tipo { NEUTRO, INFO, EXITO, ERROR }

    private final JLabel mensaje = new JLabel(" ");
    private final JProgressBar progreso = new JProgressBar();
    private final FranjaAcento franja = new FranjaAcento();

    public PanelEstado() {
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 14));

        mensaje.setFont(Tema.fuenteSubtitulo());
        progreso.setIndeterminate(true);
        progreso.setVisible(false);
        progreso.setPreferredSize(new java.awt.Dimension(90, 6));
        progreso.setBorderPainted(false);
        progreso.setForeground(Tema.PRIMARIO);

        add(franja, BorderLayout.WEST);
        add(mensaje, BorderLayout.CENTER);
        add(progreso, BorderLayout.EAST);

        mostrar(Tipo.NEUTRO, "Ingresa una cedula para comenzar.");
    }

    public void mostrar(Tipo tipo, String texto) {
        progreso.setVisible(false);
        aplicarColores(tipo);
        mensaje.setText(texto);
    }

    public void mostrarCargando(String texto) {
        aplicarColores(Tipo.INFO);
        mensaje.setText(texto);
        progreso.setVisible(true);
    }

    private void aplicarColores(Tipo tipo) {
        Color fondo;
        Color acento;
        Color textoColor;
        switch (tipo) {
            case EXITO:
                fondo = Tema.EXITO_FONDO; acento = Tema.EXITO; textoColor = Tema.EXITO;
                break;
            case ERROR:
                fondo = Tema.ERROR_FONDO; acento = Tema.ERROR; textoColor = Tema.ERROR;
                break;
            case INFO:
                fondo = Tema.INFO_FONDO; acento = Tema.INFO; textoColor = Tema.TEXTO_PRIMARIO;
                break;
            default:
                fondo = Tema.NEUTRO_FONDO; acento = Tema.NEUTRO; textoColor = Tema.TEXTO_SECUNDARIO;
        }
        setBackground(fondo);
        franja.setColor(acento);
        mensaje.setForeground(textoColor);
        setOpaque(true);
        repaint();
    }

    /** Franja vertical delgada de color de acento a la izquierda del banner. */
    private static final class FranjaAcento extends JPanel {
        private Color color = Tema.NEUTRO;

        FranjaAcento() {
            setPreferredSize(new java.awt.Dimension(4, 1));
            setOpaque(false);
        }

        void setColor(Color color) {
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
            g2.dispose();
        }
    }
}
