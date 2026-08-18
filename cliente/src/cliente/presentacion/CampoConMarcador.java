package cliente.presentacion;

import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;

/**
 * JTextField con texto de marcador (placeholder) que desaparece al recibir
 * el foco y reaparece si se deja vacio, sin depender de ninguna libreria
 * externa. {@link #textoReal()} siempre devuelve la cadena vacia mientras
 * se muestra el marcador, para que el resto del codigo nunca lo confunda
 * con una cedula ingresada por el usuario.
 */
public final class CampoConMarcador extends JTextField {
    private final String marcador;
    private boolean mostrandoMarcador = true;

    public CampoConMarcador(String marcador, int columnas) {
        super(columnas);
        this.marcador = marcador;
        setFont(Tema.fuenteCampo());
        setBorder(borde(false));
        aplicarMarcador();
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(borde(true));
                if (mostrandoMarcador) {
                    setText("");
                    setForeground(Tema.TEXTO_PRIMARIO);
                    setFont(Tema.fuenteCampo().deriveFont(Font.PLAIN));
                    mostrandoMarcador = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                setBorder(borde(false));
                if (getText().isEmpty()) {
                    aplicarMarcador();
                }
            }
        });
    }

    private static Border borde(boolean enfocado) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(enfocado ? Tema.BORDE_ENFOQUE : Tema.BORDE, enfocado ? 2 : 1),
                BorderFactory.createEmptyBorder(7, 9, 7, 9));
    }

    private void aplicarMarcador() {
        mostrandoMarcador = true;
        setText(marcador);
        setForeground(Tema.TEXTO_SECUNDARIO);
    }

    /** Texto realmente ingresado por el usuario; nunca devuelve el marcador. */
    public String textoReal() {
        return mostrandoMarcador ? "" : getText();
    }

    /** Establece un valor real (por ejemplo, desde el historial), reemplazando el marcador si estaba visible. */
    public void establecerTexto(String texto) {
        mostrandoMarcador = false;
        setForeground(Tema.TEXTO_PRIMARIO);
        setFont(Tema.fuenteCampo().deriveFont(Font.PLAIN));
        setText(texto);
    }

    /** Limpia el campo y vuelve a mostrar el marcador. */
    public void limpiar() {
        aplicarMarcador();
    }
}
