package cliente.presentacion;

import cliente.comunicacion.ClienteHTTP;
import cliente.comunicacion.ClienteTCP;
import cliente.config.Configuracion;
import cliente.dto.PersonaDTO;
import cliente.servicio.EntradaHistorial;
import cliente.servicio.HistorialConsultas;
import cliente.servicio.Protocolo;
import cliente.servicio.ResultadoConsulta;
import cliente.servicio.ServicioConsulta;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.LocalTime;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

/**
 * Interfaz grafica principal (Swing, Java estandar, sin dependencias
 * externas). Valida localmente la cedula y delega toda consulta en
 * ServicioConsulta: esta clase no abre sockets, no hace peticiones HTTP y no
 * interpreta JSON directamente, tal como exige la restriccion de
 * arquitectura del enunciado de la Etapa 2.
 *
 * El diseno visual (colores, tipografia, tarjetas, banner de estado) vive en
 * las clases auxiliares de este mismo paquete (Tema, TarjetaPanel,
 * BotonEstilizado, PanelEstado, CampoConMarcador); esta clase solo las
 * compone y sigue sin conocer nada de red ni de JSON.
 */
public final class VentanaPrincipal extends JFrame {
    private static final String[] ETIQUETAS_PERSONALES = {"Cedula", "Nombre", "Primer apellido", "Segundo apellido"};
    private static final String[] ETIQUETAS_TERRITORIALES = {"Codigo electoral", "Provincia", "Canton", "Distrito"};
    private static final String SIN_DATO = "—";

    private final ServicioConsulta servicioConsulta;
    private final HistorialConsultas historial = new HistorialConsultas();

    private final CampoConMarcador campoCedula = new CampoConMarcador("Ej: 118760457", 16);
    private final JComboBox<Protocolo> selectorProtocolo = new JComboBox<>(Protocolo.values());
    private final BotonEstilizado botonConsultar = new BotonEstilizado("Consultar", BotonEstilizado.Variante.PRIMARIO);
    private final BotonEstilizado botonHistorial = new BotonEstilizado("Historial", BotonEstilizado.Variante.SECUNDARIO);
    private final BotonEstilizado botonLimpiar = new BotonEstilizado("Limpiar", BotonEstilizado.Variante.SECUNDARIO);
    private final BotonEstilizado botonSalir = new BotonEstilizado("Salir", BotonEstilizado.Variante.TEXTO);
    private final PanelEstado panelEstado = new PanelEstado();

    private final JTextField valorCedula = campoResultado();
    private final JTextField valorNombre = campoResultado();
    private final JTextField valorPrimerApellido = campoResultado();
    private final JTextField valorSegundoApellido = campoResultado();
    private final JTextField valorCodigoElectoral = campoResultado();
    private final JTextField valorProvincia = campoResultado();
    private final JTextField valorCanton = campoResultado();
    private final JTextField valorDistrito = campoResultado();

    public VentanaPrincipal(Configuracion configuracion) {
        super("Consulta del Padron Electoral");
        ClienteTCP clienteTcp = new ClienteTCP(configuracion.servidor(), configuracion.puertoTcp(),
                configuracion.tiempoEsperaMs());
        ClienteHTTP clienteHttp = new ClienteHTTP(configuracion.servidor(), configuracion.puertoHttp(),
                configuracion.tiempoEsperaMs());
        this.servicioConsulta = new ServicioConsulta(clienteTcp, clienteHttp);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel raiz = (JPanel) getContentPane();
        raiz.setBackground(Tema.FONDO);

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);

        botonConsultar.addActionListener(e -> ejecutarConsulta());
        botonHistorial.addActionListener(e -> mostrarHistorial());
        botonLimpiar.addActionListener(e -> limpiar());
        botonSalir.addActionListener(e -> dispose());
        campoCedula.addActionListener(e -> ejecutarConsulta());

        limpiarResultados();
        setPreferredSize(new Dimension(660, 700));
        pack();
        setMinimumSize(new Dimension(560, 560));
        setLocationRelativeTo(null);
    }

    // ---- Encabezado -----------------------------------------------------

    private JComponent construirEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(14, 0));
        encabezado.setBackground(Tema.PRIMARIO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));

        encabezado.add(new Monograma(), BorderLayout.WEST);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Consulta del Padron Electoral");
        titulo.setFont(Tema.fuenteTitulo());
        titulo.setForeground(Tema.TEXTO_SOBRE_PRIMARIO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitulo = new JLabel("Verificacion de datos electorales — Etapa 2");
        subtitulo.setFont(Tema.fuenteSubtitulo());
        subtitulo.setForeground(Tema.TEXTO_SOBRE_PRIMARIO_TENUE);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(subtitulo);

        encabezado.add(textos, BorderLayout.CENTER);
        return encabezado;
    }

    /** Pequeno monograma circular ("PE") pintado a mano, sin imagenes externas. */
    private static final class Monograma extends JComponent {
        Monograma() { setPreferredSize(new Dimension(46, 46)); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Tema.PRIMARIO_OSCURO);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(Tema.TEXTO_SOBRE_PRIMARIO);
            g2.setFont(Tema.fuente(Font.BOLD, 15));
            String texto = "PE";
            var metrica = g2.getFontMetrics();
            int x = (getWidth() - metrica.stringWidth(texto)) / 2;
            int y = (getHeight() - metrica.getHeight()) / 2 + metrica.getAscent();
            g2.drawString(texto, x, y);
            g2.dispose();
        }
    }

    // ---- Cuerpo -----------------------------------------------------------

    private JComponent construirCuerpo() {
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, Tema.ESPACIO * 2, 0);

        c.gridy = 0;
        cuerpo.add(construirTarjetaConsulta(), c);

        c.gridy = 1;
        cuerpo.add(panelEstado, c);

        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        cuerpo.add(construirTarjetaResultado(), c);

        JScrollPane desplazable = new JScrollPane(cuerpo,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        desplazable.setBorder(BorderFactory.createEmptyBorder());
        desplazable.getViewport().setBackground(Tema.FONDO);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);
        return desplazable;
    }

    private JComponent construirTarjetaConsulta() {
        TarjetaPanel tarjeta = new TarjetaPanel();
        tarjeta.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, Tema.ESPACIO, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;

        c.gridy = 0;
        JLabel encabezadoTarjeta = new JLabel("Buscar persona");
        encabezadoTarjeta.setFont(Tema.fuenteSeccion());
        encabezadoTarjeta.setForeground(Tema.TEXTO_PRIMARIO);
        tarjeta.add(encabezadoTarjeta, c);

        c.gridy = 1;
        c.insets = new Insets(2, 0, 2, 0);
        tarjeta.add(etiqueta("Cedula"), c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, Tema.ESPACIO, 0);
        tarjeta.add(campoCedula, c);

        c.gridy = 3;
        c.insets = new Insets(2, 0, 2, 0);
        tarjeta.add(etiqueta("Protocolo"), c);
        c.gridy = 4;
        c.insets = new Insets(0, 0, Tema.ESPACIO * 2, 0);
        selectorProtocolo.setFont(Tema.fuenteCampo());
        selectorProtocolo.setBackground(Tema.TARJETA);
        selectorProtocolo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE, 1), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tarjeta.add(selectorProtocolo, c);

        c.gridy = 5;
        c.insets = new Insets(0, 0, 0, 0);
        tarjeta.add(construirFilaBotones(), c);

        return tarjeta;
    }

    private JComponent construirFilaBotones() {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);

        JPanel izquierda = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, Tema.ESPACIO, 0));
        izquierda.setOpaque(false);
        izquierda.add(botonConsultar);
        izquierda.add(botonHistorial);
        izquierda.add(botonLimpiar);

        JPanel derecha = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(botonSalir);

        fila.add(izquierda, BorderLayout.WEST);
        fila.add(derecha, BorderLayout.EAST);
        return fila;
    }

    private JComponent construirTarjetaResultado() {
        TarjetaPanel tarjeta = new TarjetaPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));

        JLabel encabezadoTarjeta = new JLabel("Resultado de la consulta");
        encabezadoTarjeta.setFont(Tema.fuenteSeccion());
        encabezadoTarjeta.setForeground(Tema.TEXTO_PRIMARIO);
        encabezadoTarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(encabezadoTarjeta);
        tarjeta.add(Box.createVerticalStrut(Tema.ESPACIO * 2));

        tarjeta.add(construirGrupoCampos("Datos personales",
                ETIQUETAS_PERSONALES,
                new JTextField[]{valorCedula, valorNombre, valorPrimerApellido, valorSegundoApellido}));
        tarjeta.add(Box.createVerticalStrut(Tema.ESPACIO * 2));
        tarjeta.add(separador());
        tarjeta.add(Box.createVerticalStrut(Tema.ESPACIO * 2));
        tarjeta.add(construirGrupoCampos("Datos territoriales",
                ETIQUETAS_TERRITORIALES,
                new JTextField[]{valorCodigoElectoral, valorProvincia, valorCanton, valorDistrito}));

        return tarjeta;
    }

    private JComponent construirGrupoCampos(String titulo, String[] etiquetas, JTextField[] campos) {
        JPanel grupo = new JPanel();
        grupo.setOpaque(false);
        grupo.setLayout(new BoxLayout(grupo, BoxLayout.Y_AXIS));
        grupo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tituloGrupo = new JLabel(titulo.toUpperCase());
        tituloGrupo.setFont(Tema.fuenteEtiqueta().deriveFont(Font.BOLD));
        tituloGrupo.setForeground(Tema.TEXTO_SECUNDARIO);
        tituloGrupo.setAlignmentX(Component.LEFT_ALIGNMENT);
        grupo.add(tituloGrupo);
        grupo.add(Box.createVerticalStrut(Tema.ESPACIO));

        JPanel rejilla = new JPanel(new GridLayout(0, 2, 24, 14));
        rejilla.setOpaque(false);
        rejilla.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < etiquetas.length; i++) {
            rejilla.add(construirCeldaCampo(etiquetas[i], campos[i]));
        }
        grupo.add(rejilla);
        return grupo;
    }

    private JComponent construirCeldaCampo(String etiquetaTexto, JTextField campo) {
        JPanel celda = new JPanel();
        celda.setOpaque(false);
        celda.setLayout(new BoxLayout(celda, BoxLayout.Y_AXIS));
        JLabel etiqueta = etiqueta(etiquetaTexto);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        celda.add(etiqueta);
        celda.add(campo);
        return celda;
    }

    private static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(Tema.fuenteEtiqueta());
        etiqueta.setForeground(Tema.TEXTO_SECUNDARIO);
        return etiqueta;
    }

    private static JComponent separador() {
        JPanel linea = new JPanel();
        linea.setBackground(Tema.BORDE);
        linea.setPreferredSize(new Dimension(10, 1));
        linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);
        return linea;
    }

    private static JTextField campoResultado() {
        JTextField campo = new JTextField();
        campo.setEditable(false);
        campo.setFocusable(false);
        campo.setOpaque(false);
        campo.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        campo.setFont(Tema.fuenteValor());
        campo.setForeground(Tema.TEXTO_PRIMARIO);
        return campo;
    }

    // ---- Logica de interaccion (sin cambios de arquitectura) --------------

    private void ejecutarConsulta() {
        String cedula = campoCedula.textoReal();
        String errorLocal = ServicioConsulta.validarCedula(cedula);
        if (errorLocal != null) {
            panelEstado.mostrar(PanelEstado.Tipo.ERROR, errorLocal);
            return;
        }
        Protocolo protocolo = (Protocolo) selectorProtocolo.getSelectedItem();
        establecerHabilitado(false);
        panelEstado.mostrarCargando("Consultando por " + protocolo + "...");
        new SwingWorker<ResultadoConsulta, Void>() {
            @Override
            protected ResultadoConsulta doInBackground() {
                return servicioConsulta.consultar(cedula, protocolo);
            }

            @Override
            protected void done() {
                establecerHabilitado(true);
                try {
                    ResultadoConsulta resultado = get();
                    registrarHistorial(cedula, protocolo, resultado);
                    mostrarResultado(resultado);
                } catch (Exception e) {
                    panelEstado.mostrar(PanelEstado.Tipo.ERROR, "Error inesperado en la interfaz: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void mostrarHistorial() {
        new VentanaHistorial(this, historial, entrada -> {
            campoCedula.establecerTexto(entrada.cedula());
            selectorProtocolo.setSelectedItem(entrada.protocolo());
        }).setVisible(true);
    }

    private void registrarHistorial(String cedula, Protocolo protocolo, ResultadoConsulta resultado) {
        String resumen = resultado.exito()
                ? String.join(" ", resultado.persona().nombre(), resultado.persona().primerApellido(),
                        resultado.persona().segundoApellido())
                : resultado.error().mensaje();
        historial.registrar(new EntradaHistorial(LocalTime.now(), cedula, protocolo, resultado.exito(), resumen));
    }

    private void mostrarResultado(ResultadoConsulta resultado) {
        if (resultado.exito()) {
            PersonaDTO persona = resultado.persona();
            establecerValor(valorCedula, persona.cedula());
            establecerValor(valorNombre, persona.nombre());
            establecerValor(valorPrimerApellido, persona.primerApellido());
            establecerValor(valorSegundoApellido, persona.segundoApellido());
            establecerValor(valorCodigoElectoral, persona.codigoElectoral());
            establecerValor(valorProvincia, persona.provincia());
            establecerValor(valorCanton, persona.canton());
            establecerValor(valorDistrito, persona.distrito());
            panelEstado.mostrar(PanelEstado.Tipo.EXITO, "Consulta exitosa.");
        } else {
            limpiarResultados();
            panelEstado.mostrar(PanelEstado.Tipo.ERROR, resultado.error().mensaje());
        }
    }

    private void establecerHabilitado(boolean habilitado) {
        botonConsultar.setEnabled(habilitado);
        campoCedula.setEnabled(habilitado);
        selectorProtocolo.setEnabled(habilitado);
    }

    private void limpiar() {
        campoCedula.limpiar();
        limpiarResultados();
        panelEstado.mostrar(PanelEstado.Tipo.NEUTRO, "Ingresa una cedula para comenzar.");
        campoCedula.requestFocusInWindow();
    }

    private void limpiarResultados() {
        for (Component campo : new Component[]{valorCedula, valorNombre, valorPrimerApellido,
                valorSegundoApellido, valorCodigoElectoral, valorProvincia, valorCanton, valorDistrito}) {
            limpiarValor((JTextField) campo);
        }
    }

    private static void establecerValor(JTextField campo, String texto) {
        campo.setText(texto == null || texto.isBlank() ? SIN_DATO : texto);
        campo.setForeground(Tema.TEXTO_PRIMARIO);
        campo.setFont(Tema.fuenteValor());
    }

    private static void limpiarValor(JTextField campo) {
        campo.setText(SIN_DATO);
        campo.setForeground(Tema.TEXTO_SECUNDARIO);
        campo.setFont(Tema.fuenteValor().deriveFont(Font.ITALIC));
    }
}
