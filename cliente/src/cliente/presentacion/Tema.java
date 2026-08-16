package cliente.presentacion;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

/**
 * Paleta de colores, tipografia y espaciados centralizados de la interfaz.
 * Se concentran aqui todas las decisiones visuales para que los componentes
 * de presentacion (VentanaPrincipal, tarjetas, botones, banner de estado)
 * sean consistentes entre si sin repetir valores sueltos por toda la clase.
 *
 * No depende de ninguna biblioteca externa: todo se resuelve con
 * java.awt.Color/Font, respetando la restriccion del proyecto.
 */
public final class Tema {
    private Tema() { }

    // Paleta institucional: azul profundo como color primario, gris neutro
    // de fondo y acentos discretos para exito/error, evitando el aspecto
    // por defecto de Swing (gris de sistema operativo).
    public static final Color PRIMARIO = new Color(0x1B, 0x3A, 0x63);
    public static final Color PRIMARIO_HOVER = new Color(0x24, 0x4A, 0x7A);
    public static final Color PRIMARIO_PRESIONADO = new Color(0x14, 0x2C, 0x4B);
    public static final Color PRIMARIO_OSCURO = new Color(0x0F, 0x22, 0x3B);

    public static final Color FONDO = new Color(0xF2, 0xF4, 0xF8);
    public static final Color TARJETA = Color.WHITE;
    public static final Color BORDE = new Color(0xDC, 0xE2, 0xE9);
    public static final Color BORDE_ENFOQUE = PRIMARIO;

    public static final Color TEXTO_PRIMARIO = new Color(0x1F, 0x29, 0x37);
    public static final Color TEXTO_SECUNDARIO = new Color(0x63, 0x6D, 0x7A);
    public static final Color TEXTO_SOBRE_PRIMARIO = Color.WHITE;
    public static final Color TEXTO_SOBRE_PRIMARIO_TENUE = new Color(0xC7, 0xD3, 0xE3);

    public static final Color BOTON_SECUNDARIO_FONDO = new Color(0xE8, 0xEC, 0xF1);
    public static final Color BOTON_SECUNDARIO_FONDO_HOVER = new Color(0xDC, 0xE1, 0xE9);
    public static final Color BOTON_SECUNDARIO_TEXTO = TEXTO_PRIMARIO;

    public static final Color EXITO = new Color(0x1E, 0x7A, 0x4C);
    public static final Color EXITO_FONDO = new Color(0xE6, 0xF4, 0xEB);
    public static final Color ERROR = new Color(0xB0, 0x33, 0x28);
    public static final Color ERROR_FONDO = new Color(0xFB, 0xE9, 0xE7);
    public static final Color INFO = PRIMARIO;
    public static final Color INFO_FONDO = new Color(0xE9, 0xEE, 0xF6);
    public static final Color NEUTRO = TEXTO_SECUNDARIO;
    public static final Color NEUTRO_FONDO = new Color(0xEE, 0xF0, 0xF3);

    public static final int RADIO_TARJETA = 12;
    public static final int RADIO_BOTON = 8;
    public static final int RADIO_CAMPO = 6;
    public static final int ESPACIO = 8;

    private static final List<String> FAMILIAS_PREFERIDAS =
            Arrays.asList("Segoe UI", "Inter", "SansSerif");
    private static volatile String familiaDisponible;

    private static String familia() {
        if (familiaDisponible == null) {
            List<String> instaladas = Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            familiaDisponible = FAMILIAS_PREFERIDAS.stream()
                    .filter(instaladas::contains)
                    .findFirst()
                    .orElse(Font.SANS_SERIF);
        }
        return familiaDisponible;
    }

    public static Font fuente(int estilo, int tamano) {
        return new Font(familia(), estilo, tamano);
    }

    public static Font fuenteTitulo() { return fuente(Font.BOLD, 20); }
    public static Font fuenteSubtitulo() { return fuente(Font.PLAIN, 13); }
    public static Font fuenteSeccion() { return fuente(Font.BOLD, 12); }
    public static Font fuenteEtiqueta() { return fuente(Font.PLAIN, 11); }
    public static Font fuenteValor() { return fuente(Font.PLAIN, 14); }
    public static Font fuenteValorEnfasis() { return fuente(Font.BOLD, 14); }
    public static Font fuenteBoton() { return fuente(Font.BOLD, 13); }
    public static Font fuenteCampo() { return fuente(Font.PLAIN, 14); }
}
