package cliente.presentacion;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Boton pintado a mano con Graphics2D (esquinas redondeadas, estados de
 * hover/presionado propios) en vez de depender del Look &amp; Feel nativo del
 * sistema operativo, que en Swing no permite recolorear de forma confiable
 * los botones por defecto. No usa ninguna libreria externa: solo AWT/Swing
 * estandar.
 */
public final class BotonEstilizado extends JButton {

    /** Variantes visuales disponibles, de mayor a menor enfasis. */
    public enum Variante { PRIMARIO, SECUNDARIO, TEXTO }

    private final Variante variante;
    private boolean sobrePuntero = false;
    private boolean presionado = false;

    public BotonEstilizado(String texto, Variante variante) {
        super(texto);
        this.variante = variante;
        setFont(Tema.fuenteBoton());
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.CENTER);
        int padV = variante == Variante.TEXTO ? 6 : 10;
        int padH = variante == Variante.TEXTO ? 10 : 18;
        setBorder(javax.swing.BorderFactory.createEmptyBorder(padV, padH, padV, padH));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { sobrePuntero = true; repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { sobrePuntero = false; repaint(); }
            @Override
            public void mousePressed(MouseEvent e) { presionado = true; repaint(); }
            @Override
            public void mouseReleased(MouseEvent e) { presionado = false; repaint(); }
        });
    }

    private Color colorFondo() {
        if (!isEnabled()) {
            return variante == Variante.TEXTO ? null : Tema.NEUTRO_FONDO;
        }
        switch (variante) {
            case PRIMARIO:
                return presionado ? Tema.PRIMARIO_PRESIONADO : sobrePuntero ? Tema.PRIMARIO_HOVER : Tema.PRIMARIO;
            case SECUNDARIO:
                return sobrePuntero ? Tema.BOTON_SECUNDARIO_FONDO_HOVER : Tema.BOTON_SECUNDARIO_FONDO;
            default:
                return sobrePuntero ? Tema.NEUTRO_FONDO : null;
        }
    }

    private Color colorTexto() {
        if (!isEnabled()) {
            return Tema.TEXTO_SECUNDARIO;
        }
        return variante == Variante.PRIMARIO ? Tema.TEXTO_SOBRE_PRIMARIO : Tema.BOTON_SECUNDARIO_TEXTO;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fondo = colorFondo();
        if (fondo != null) {
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), Tema.RADIO_BOTON, Tema.RADIO_BOTON);
        }
        g2.dispose();
        setForeground(colorTexto());
        super.paintComponent(g);
    }
}
