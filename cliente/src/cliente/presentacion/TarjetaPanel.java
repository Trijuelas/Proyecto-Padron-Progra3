package cliente.presentacion;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Panel tipo "tarjeta": fondo blanco con esquinas redondeadas y borde sutil
 * sobre el fondo gris de la ventana, en vez del TitledBorder por defecto de
 * Swing. Pintado con Graphics2D estandar, sin dependencias externas.
 */
public final class TarjetaPanel extends JPanel {

    public TarjetaPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Tema.TARJETA);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Tema.RADIO_TARJETA, Tema.RADIO_TARJETA);
        g2.setColor(Tema.BORDE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Tema.RADIO_TARJETA, Tema.RADIO_TARJETA);
        g2.dispose();
        super.paintComponent(g);
    }
}
