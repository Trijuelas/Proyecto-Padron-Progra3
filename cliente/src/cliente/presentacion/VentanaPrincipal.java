package cliente.presentacion;

import cliente.comunicacion.ClienteHTTP;
import cliente.comunicacion.ClienteTCP;
import cliente.config.Configuracion;
import cliente.dto.PersonaDTO;
import cliente.servicio.Protocolo;
import cliente.servicio.ResultadoConsulta;
import cliente.servicio.ServicioConsulta;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

/**
 * Interfaz grafica principal (Swing, Java estandar, sin dependencias
 * externas). Valida localmente la cedula y delega toda consulta en
 * ServicioConsulta: esta clase no abre sockets, no hace peticiones HTTP y no
 * interpreta JSON directamente, tal como exige la restriccion de
 * arquitectura del enunciado de la Etapa 2.
 */
public final class VentanaPrincipal extends JFrame {
    private final ServicioConsulta servicioConsulta;

    private final JTextField campoCedula = new JTextField(14);
    private final JComboBox<Protocolo> selectorProtocolo = new JComboBox<>(Protocolo.values());
    private final JButton botonConsultar = new JButton("Consultar");
    private final JButton botonLimpiar = new JButton("Limpiar");
    private final JButton botonSalir = new JButton("Salir");
    private final JLabel estado = new JLabel(" ");

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
        setLayout(new BorderLayout(10, 10));
        JPanel contenido = (JPanel) getContentPane();
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(panelConsulta(), BorderLayout.NORTH);
        add(panelResultados(), BorderLayout.CENTER);
        add(panelInferior(), BorderLayout.SOUTH);

        botonConsultar.addActionListener(e -> ejecutarConsulta());
        botonLimpiar.addActionListener(e -> limpiar());
        botonSalir.addActionListener(e -> dispose());
        campoCedula.addActionListener(e -> ejecutarConsulta());

        pack();
        setMinimumSize(new Dimension(480, 430));
        setLocationRelativeTo(null);
    }

    private JPanel panelConsulta() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Consulta"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Cedula:"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campoCedula, c);

        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        panel.add(new JLabel("Protocolo:"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(selectorProtocolo, c);

        return panel;
    }

    private JPanel panelResultados() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Resultado"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        String[] etiquetas = {"Cedula", "Nombre", "Primer apellido", "Segundo apellido",
                "Codigo electoral", "Provincia", "Canton", "Distrito"};
        JTextField[] campos = {valorCedula, valorNombre, valorPrimerApellido, valorSegundoApellido,
                valorCodigoElectoral, valorProvincia, valorCanton, valorDistrito};
        for (int fila = 0; fila < etiquetas.length; fila++) {
            c.gridx = 0;
            c.gridy = fila;
            c.weightx = 0;
            panel.add(new JLabel(etiquetas[fila] + ":"), c);
            c.gridx = 1;
            c.weightx = 1;
            panel.add(campos[fila], c);
        }
        return panel;
    }

    private JPanel panelInferior() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel botones = new JPanel();
        botones.add(botonConsultar);
        botones.add(botonLimpiar);
        botones.add(botonSalir);
        panel.add(botones, BorderLayout.NORTH);
        estado.setHorizontalAlignment(JLabel.CENTER);
        panel.add(estado, BorderLayout.SOUTH);
        return panel;
    }

    private static JTextField campoResultado() {
        JTextField campo = new JTextField();
        campo.setEditable(false);
        campo.setFocusable(false);
        return campo;
    }

    private void ejecutarConsulta() {
        String cedula = campoCedula.getText();
        String errorLocal = ServicioConsulta.validarCedula(cedula);
        if (errorLocal != null) {
            mostrarEstado(errorLocal, true);
            return;
        }
        Protocolo protocolo = (Protocolo) selectorProtocolo.getSelectedItem();
        establecerHabilitado(false);
        mostrarEstado("Consultando por " + protocolo + "...", false);
        new SwingWorker<ResultadoConsulta, Void>() {
            @Override
            protected ResultadoConsulta doInBackground() {
                return servicioConsulta.consultar(cedula, protocolo);
            }

            @Override
            protected void done() {
                establecerHabilitado(true);
                try {
                    mostrarResultado(get());
                } catch (Exception e) {
                    mostrarEstado("Error inesperado en la interfaz: " + e.getMessage(), true);
                }
            }
        }.execute();
    }

    private void mostrarResultado(ResultadoConsulta resultado) {
        if (resultado.exito()) {
            PersonaDTO persona = resultado.persona();
            valorCedula.setText(persona.cedula());
            valorNombre.setText(persona.nombre());
            valorPrimerApellido.setText(persona.primerApellido());
            valorSegundoApellido.setText(persona.segundoApellido());
            valorCodigoElectoral.setText(persona.codigoElectoral());
            valorProvincia.setText(persona.provincia());
            valorCanton.setText(persona.canton());
            valorDistrito.setText(persona.distrito());
            mostrarEstado("Consulta exitosa.", false);
        } else {
            limpiarResultados();
            mostrarEstado(resultado.error().mensaje(), true);
        }
    }

    private void mostrarEstado(String mensaje, boolean esError) {
        estado.setText(mensaje);
        estado.setForeground(esError ? Color.RED.darker() : new Color(0, 110, 0));
    }

    private void establecerHabilitado(boolean habilitado) {
        botonConsultar.setEnabled(habilitado);
        campoCedula.setEnabled(habilitado);
        selectorProtocolo.setEnabled(habilitado);
    }

    private void limpiar() {
        campoCedula.setText("");
        limpiarResultados();
        mostrarEstado(" ", false);
        campoCedula.requestFocusInWindow();
    }

    private void limpiarResultados() {
        for (Component campo : new Component[]{valorCedula, valorNombre, valorPrimerApellido,
                valorSegundoApellido, valorCodigoElectoral, valorProvincia, valorCanton, valorDistrito}) {
            ((JTextField) campo).setText("");
        }
    }
}
