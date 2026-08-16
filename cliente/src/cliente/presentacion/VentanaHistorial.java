package cliente.presentacion;

import cliente.servicio.EntradaHistorial;
import cliente.servicio.HistorialConsultas;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * Dialogo con el historial de consultas de la sesion actual. Solo lee de
 * HistorialConsultas y notifica al llamador cuando el usuario elige una
 * entrada; no ejecuta ninguna consulta ni conoce ServicioConsulta, tal como
 * exige la separacion de capas del proyecto.
 */
public final class VentanaHistorial extends JDialog {
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final HistorialConsultas historial;
    private final DefaultListModel<EntradaHistorial> modelo = new DefaultListModel<>();
    private final JList<EntradaHistorial> lista = new JList<>(modelo);
    private final JPanel contenedor = new JPanel(new BorderLayout());
    private final JLabel vacio = new JLabel("Aun no has hecho ninguna consulta en esta sesion.", SwingConstants.CENTER);

    public VentanaHistorial(JFrame propietario, HistorialConsultas historial, Consumer<EntradaHistorial> alSeleccionar) {
        super(propietario, "Historial de consultas", true);
        this.historial = historial;

        ((JPanel) getContentPane()).setBackground(Tema.FONDO);
        setLayout(new BorderLayout());

        add(construirEncabezado(), BorderLayout.NORTH);

        lista.setModel(modelo);
        lista.setCellRenderer(new CeldaHistorial());
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(Tema.TARJETA);
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int indice = lista.locationToIndex(e.getPoint());
                if (indice >= 0) {
                    alSeleccionar.accept(modelo.get(indice));
                    dispose();
                }
            }
        });

        vacio.setFont(Tema.fuenteSubtitulo());
        vacio.setForeground(Tema.TEXTO_SECUNDARIO);
        vacio.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        contenedor.setBackground(Tema.FONDO);
        contenedor.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        add(contenedor, BorderLayout.CENTER);

        add(construirPie(), BorderLayout.SOUTH);

        refrescar();
        setPreferredSize(new Dimension(480, 440));
        pack();
        setLocationRelativeTo(propietario);
    }

    private JPanel construirEncabezado() {
        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setBackground(Tema.FONDO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));

        JLabel titulo = new JLabel("Historial de consultas");
        titulo.setFont(Tema.fuenteSeccion());
        titulo.setForeground(Tema.TEXTO_PRIMARIO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ayuda = new JLabel("Solo de esta sesion. Haz clic en una fila para rellenar el formulario.");
        ayuda.setFont(Tema.fuenteEtiqueta());
        ayuda.setForeground(Tema.TEXTO_SECUNDARIO);
        ayuda.setAlignmentX(Component.LEFT_ALIGNMENT);

        encabezado.add(titulo);
        encabezado.add(javax.swing.Box.createVerticalStrut(4));
        encabezado.add(ayuda);
        return encabezado;
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(Tema.FONDO);
        pie.setBorder(BorderFactory.createEmptyBorder(10, 20, 16, 20));

        BotonEstilizado botonLimpiar = new BotonEstilizado("Limpiar historial", BotonEstilizado.Variante.TEXTO);
        botonLimpiar.addActionListener(e -> {
            historial.limpiar();
            refrescar();
        });
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izquierda.setOpaque(false);
        izquierda.add(botonLimpiar);

        BotonEstilizado botonCerrar = new BotonEstilizado("Cerrar", BotonEstilizado.Variante.SECUNDARIO);
        botonCerrar.addActionListener(e -> dispose());
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(botonCerrar);

        pie.add(izquierda, BorderLayout.WEST);
        pie.add(derecha, BorderLayout.EAST);
        return pie;
    }

    private void refrescar() {
        List<EntradaHistorial> entradas = historial.obtenerTodo();
        contenedor.removeAll();
        if (entradas.isEmpty()) {
            contenedor.add(vacio, BorderLayout.CENTER);
        } else {
            modelo.clear();
            entradas.forEach(modelo::addElement);
            JScrollPane desplazable = new JScrollPane(lista,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            desplazable.setBorder(BorderFactory.createLineBorder(Tema.BORDE, 1));
            contenedor.add(desplazable, BorderLayout.CENTER);
        }
        contenedor.revalidate();
        contenedor.repaint();
    }

    /** Fila del historial: hora/cedula/protocolo a la izquierda, estado a la derecha. */
    private static final class CeldaHistorial extends JPanel implements ListCellRenderer<EntradaHistorial> {
        private final JLabel principal = new JLabel();
        private final JLabel resumen = new JLabel();
        private final JLabel estado = new JLabel();

        CeldaHistorial() {
            setLayout(new BorderLayout(10, 2));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));

            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            principal.setFont(Tema.fuenteValor());
            resumen.setFont(Tema.fuenteEtiqueta());
            textos.add(principal);
            textos.add(resumen);

            estado.setFont(Tema.fuenteEtiqueta().deriveFont(Font.BOLD));
            estado.setVerticalAlignment(SwingConstants.TOP);

            add(textos, BorderLayout.CENTER);
            add(estado, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends EntradaHistorial> list, EntradaHistorial valor,
                int index, boolean seleccionado, boolean conFoco) {
            principal.setText(valor.hora().format(FORMATO_HORA) + "   Cedula " + valor.cedula() + "   (" + valor.protocolo() + ")");
            principal.setForeground(Tema.TEXTO_PRIMARIO);
            resumen.setText(valor.resumen());
            resumen.setForeground(Tema.TEXTO_SECUNDARIO);
            estado.setText(valor.exito() ? "Exito" : "Error");
            estado.setForeground(valor.exito() ? Tema.EXITO : Tema.ERROR);
            setBackground(seleccionado ? Tema.INFO_FONDO : Tema.TARJETA);
            setOpaque(true);
            return this;
        }
    }
}
