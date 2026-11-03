package uni.formularios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;
import uni.controller.GestorComandos;
import uni.entity.AccionEnvio;
import uni.entity.EnvioTo;
import uni.entity.EventoEnvio;
import uni.entity.SeguimientoTo;
import uni.service.ComandoCambiarEstado;
import uni.service.ComandoConfirmarEntrega;
import uni.service.ComandoRegistrarIncidencia;
import uni.service.ComandoReprogramar;
import uni.service.EnvioMemento;
import uni.service.IComando;
import uni.service.IObservadorEnvio;
import uni.service.ISeguimientoService;
import uni.service.IServicioEnvio;
import uni.util.ConfiguracionSistema;
import uni.util.HistorialMementos;
import uni.util.ServicioEnvioFactory;

/**
 * Seguimiento y cambio de estados de un envío. Formulario del Capítulo VI:
 *  - STATE:    los botones de acción se habilitan según el estado actual.
 *  - COMMAND:  cada botón crea un comando que ejecuta GestorComandos
 *              (deshacer / rehacer / cola sin conexión).
 *  - MEMENTO:  puntos de restauración manuales (HistorialMementos).
 *  - OBSERVER: esta ventana es un observador más: muestra los eventos en vivo.
 */
public class SeguimientoView extends FormularioBase implements IObservadorEnvio {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");

    private final ISeguimientoService servicio = ConfiguracionSistema.getInstancia().getSeguimiento();
    private final GestorComandos gestor = ConfiguracionSistema.getInstancia().getGestorComandos();
    private final HistorialMementos caretaker = ConfiguracionSistema.getInstancia().getHistorialMementos();

    private final JTextField txtCodigo = new JTextField(12);
    private final JLabel lblEstado = new JLabel("-");
    private final JLabel lblServicio = new JLabel("-");
    private final JLabel lblFlujo = new JLabel("-");
    private final JLabel lblDestino = new JLabel("-");
    private final JLabel lblExtra = new JLabel("-");
    private final JTextField txtUbicacion = new JTextField("Almacén Callao", 16);
    private final JTextField txtObservacion = new JTextField(22);
    private final Map<AccionEnvio, JButton> botonesAccion = new EnumMap<>(AccionEnvio.class);
    private final JCheckBox chkSinConexion = new JCheckBox("Modo sin conexión (encolar comandos)");
    private final JButton btnDeshacer;
    private final JButton btnRehacer;
    private final JButton btnSincronizar;
    private final JLabel lblComandos = new JLabel();
    private final JLabel lblProxy = new JLabel(" ");

    private final DefaultTableModel mHistorial = modelo("Fecha y hora", "Estado", "Ubicación", "Observación", "Empleado");
    private final JTextArea txtEventos = new JTextArea();
    private final DefaultTableModel mComandos = modelo("#", "Comando ejecutado (pila deshacer)", "Estado previo", "Estado nuevo");
    private final DefaultListModel<EnvioMemento> mMementos = new DefaultListModel<>();
    private final JList<EnvioMemento> lstMementos = new JList<>(mMementos);

    public SeguimientoView(String codigoInicial) {
        super("Seguimiento y estados del envío", 1040, 680);
        getContentPane().add(cabecera("Seguimiento de envíos",
                "State (acciones válidas) · Command (deshacer/rehacer/cola) · Memento (restaurar) · Observer (eventos en vivo)"),
                BorderLayout.NORTH);

        // --- búsqueda y datos del envío
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busqueda.add(new JLabel("Código de envío:"));
        busqueda.add(txtCodigo);
        busqueda.add(boton("Buscar", "buscar.gif", e -> cargar()));
        txtCodigo.addActionListener(e -> cargar());
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 15f));
        lblEstado.setForeground(AZUL);
        JPanel datos = formulario("Envío", "Estado actual", lblEstado, "Servicio", lblServicio,
                "Flujo del servicio", lblFlujo, "Destinatario / destino", lblDestino, "Intentos / fechas", lblExtra);

        // --- acciones (State + Command)
        JPanel acciones = new JPanel(new GridLayout(0, 4, 4, 4));
        acciones.setBorder(BorderFactory.createTitledBorder("Acciones permitidas por el estado actual (State)"));
        for (AccionEnvio a : AccionEnvio.values()) {
            JButton b = new JButton(a.getEtiqueta());
            b.addActionListener(e -> ejecutarAccion(a));
            b.setEnabled(false);
            botonesAccion.put(a, b);
            acciones.add(b);
        }
        JPanel operacion = formulario(null, "Ubicación", txtUbicacion, "Observación", txtObservacion);
        JPanel panelAcciones = new JPanel(new BorderLayout());
        panelAcciones.add(acciones, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(operacion, BorderLayout.CENTER);
        lblProxy.setForeground(new Color(170, 40, 40));
        sur.add(lblProxy, BorderLayout.SOUTH);
        panelAcciones.add(sur, BorderLayout.SOUTH);

        JPanel comandos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comandos.setBorder(BorderFactory.createTitledBorder("Command / Memento"));
        btnDeshacer = boton("Deshacer", "update.png", e -> deshacer());
        btnRehacer = boton("Rehacer", "Run.png", e -> rehacer());
        btnSincronizar = boton("Sincronizar pendientes", "Globe1.png", e -> sincronizar());
        comandos.add(btnDeshacer);
        comandos.add(btnRehacer);
        comandos.add(chkSinConexion);
        comandos.add(btnSincronizar);
        comandos.add(boton("Guardar punto de restauración", "save_all.png", e -> guardarPunto()));
        comandos.add(lblComandos);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.add(busqueda, BorderLayout.NORTH);
        izquierda.add(datos, BorderLayout.CENTER);
        JPanel arriba = new JPanel(new GridLayout(1, 2, 6, 0));
        arriba.add(izquierda);
        arriba.add(panelAcciones);
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(arriba, BorderLayout.CENTER);
        norte.add(comandos, BorderLayout.SOUTH);

        // --- pestañas
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Historial de seguimiento", new JScrollPane(tabla(mHistorial)));
        txtEventos.setEditable(false);
        txtEventos.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtEventos.setBackground(new Color(250, 250, 240));
        tabs.addTab("Eventos en vivo (Observer)", new JScrollPane(txtEventos));
        JTable tc = tabla(mComandos);
        tabs.addTab("Comandos (Command)", new JScrollPane(tc));
        JPanel pm = new JPanel(new BorderLayout());
        pm.add(new JScrollPane(lstMementos), BorderLayout.CENTER);
        JPanel bm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bm.add(boton("Restaurar punto seleccionado", "update.png", e -> restaurarPunto()));
        bm.add(new JLabel("El Caretaker solo conoce la etiqueta y la hora; el contenido lo lee el Originator."));
        pm.add(bm, BorderLayout.SOUTH);
        tabs.addTab("Puntos de restauración (Memento)", pm);

        JPanel centro = new JPanel(new BorderLayout(4, 4));
        centro.add(norte, BorderLayout.NORTH);
        centro.add(tabs, BorderLayout.CENTER);
        getContentPane().add(centro, BorderLayout.CENTER);

        // --- OBSERVER: la ventana se suscribe al abrir y se desuscribe al cerrar
        ConfiguracionSistema.getInstancia().getPublicadorEventos().agregarObservador(this);
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                ConfiguracionSistema.getInstancia().getPublicadorEventos().quitarObservador(SeguimientoView.this);
            }
        });
        txtEventos.setText("Ventana suscrita como observador. Los cambios de estado de cualquier envío "
                + "aparecerán aquí.\n");

        actualizarComandos();
        if (codigoInicial != null) {
            txtCodigo.setText(codigoInicial);
            cargar();
        }
    }

    // ====================================================== carga de datos
    private void cargar() {
        String cod = txtCodigo.getText().trim().toUpperCase();
        txtCodigo.setText(cod);
        if (cod.isEmpty()) {
            return;
        }
        try {
            EnvioTo e = servicio.buscarEnvio(cod);
            if (e == null) {
                info("No existe el envío " + cod);
                limpiar();
                return;
            }
            IServicioEnvio s = new ServicioEnvioFactory().crearServicio(e.getServicio());
            lblEstado.setText(e.getEstado().name());
            lblServicio.setText(e.getServicio() + "  (prioridad " + s.getPrioridad() + ")   S/ "
                    + String.format("%.2f", e.getCosto()));
            lblFlujo.setText(flujo(s, e));
            lblDestino.setText(e.getNombreDestinatario() + " · " + (e.getDestino() == null
                    ? "Recojo en tienda " + e.getIdtiendaRecojo() : e.getDestino().toString()));
            lblExtra.setText("Intentos fallidos: " + e.getIntentos() + "/3 · Estimada: " + e.getFechaEstimada()
                    + (e.getFechaProgramada() == null ? "" : " · Programada: " + e.getFechaProgramada())
                    + (e.isRequiereFirma() ? " · Requiere firma" : "")
                    + (e.getCodigoFirma() == null ? "" : " (" + e.getCodigoFirma() + ")"));

            List<AccionEnvio> permitidas = servicio.accionesPermitidas(cod);  // State (+ Proxy)
            for (Map.Entry<AccionEnvio, JButton> b : botonesAccion.entrySet()) {
                b.getValue().setEnabled(permitidas.contains(b.getKey()));
            }
            // Si el estado admite acciones pero el Proxy no deja operar, se informa al usuario
            List<AccionEnvio> segunEstado = ConfiguracionSistema.getInstancia().getPublicadorEventos()
                    .accionesPermitidas(cod);
            lblProxy.setText(permitidas.isEmpty() && !segunEstado.isEmpty()
                    ? "Proxy: su rol o asignación no le permite operar este envío (solo consulta)." : " ");
            mHistorial.setRowCount(0);
            for (SeguimientoTo h : servicio.consultarHistorial(cod)) {
                mHistorial.addRow(new Object[]{h.getFechahora().format(HORA), h.getEstado(),
                    h.getUbicacion(), h.getObservacion(), h.getIdempleado()});
            }
            mMementos.clear();
            for (EnvioMemento m : caretaker.listar(cod)) {
                mMementos.addElement(m);
            }
        } catch (Exception ex) {
            error(ex);
        }
        actualizarComandos();
    }

    /** Muestra el flujo del servicio marcando la etapa actual. */
    private String flujo(IServicioEnvio s, EnvioTo e) {
        StringBuilder sb = new StringBuilder("<html>");
        List<?> pasos = s.getFlujoEstados();
        for (int i = 0; i < pasos.size(); i++) {
            String p = pasos.get(i).toString();
            boolean actual = i == e.getPasoFlujo();
            sb.append(i > 0 ? " → " : "").append(actual ? "<b><u>" + p + "</u></b>" : p);
        }
        return sb.append("</html>").toString();
    }

    private void limpiar() {
        lblEstado.setText("-");
        lblServicio.setText("-");
        lblFlujo.setText("-");
        lblDestino.setText("-");
        lblExtra.setText("-");
        mHistorial.setRowCount(0);
        mMementos.clear();
        for (JButton b : botonesAccion.values()) {
            b.setEnabled(false);
        }
    }

    // ============================================== acciones = comandos
    private void ejecutarAccion(AccionEnvio a) {
        String cod = txtCodigo.getText().trim();
        String ubic = txtUbicacion.getText().trim();
        String obs = txtObservacion.getText().trim().isEmpty() ? a.getEtiqueta() : txtObservacion.getText().trim();
        IComando cmd;
        switch (a) {
            case OBSERVAR: {
                JComboBox<String> motivo = new JComboBox<>(new String[]{"DIRECCION_INCORRECTA",
                    "AUSENCIA_DESTINATARIO", "CLIMA", "INCIDENTE_OPERATIVO", "OTRO"});
                if (JOptionPane.showConfirmDialog(this, motivo, "Motivo de la incidencia",
                        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                    return;
                }
                cmd = new ComandoRegistrarIncidencia(servicio, cod, (String) motivo.getSelectedItem(),
                        txtObservacion.getText().trim(), ubic);
                break;
            }
            case REPROGRAMAR: {
                String f = JOptionPane.showInputDialog(this, "Nueva fecha de entrega (aaaa-mm-dd):",
                        LocalDate.now().plusDays(1).toString());
                if (f == null) {
                    return;
                }
                try {
                    cmd = new ComandoReprogramar(servicio, cod, LocalDate.parse(f.trim()), obs);
                } catch (Exception ex) {
                    info("Fecha inválida.");
                    return;
                }
                break;
            }
            case ENTREGAR: {
                String dni = null;
                try {
                    if (servicio.buscarEnvio(cod).isRequiereFirma()) {
                        dni = JOptionPane.showInputDialog(this,
                                "El envío requiere firma digital.\nDNI de la persona que recibe:");
                        if (dni == null) {
                            return;
                        }
                    }
                } catch (Exception ex) {
                    error(ex);
                    return;
                }
                cmd = new ComandoConfirmarEntrega(servicio, cod, dni == null ? null : dni.trim(), ubic);
                break;
            }
            default:
                cmd = new ComandoCambiarEstado(servicio, cod, a, ubic, obs);
        }
        if (chkSinConexion.isSelected()) {
            gestor.encolar(cmd);
            txtEventos.append("[COLA] Sin conexión: se encoló \"" + cmd.getDescripcion() + "\"\n");
            actualizarComandos();
            return;
        }
        try {
            gestor.ejecutar(cmd);
        } catch (Exception ex) {
            error(ex);
        }
        txtObservacion.setText("");
        cargar();
    }

    private void deshacer() {
        try {
            IComando c = gestor.deshacer();
            txtEventos.append("[DESHACER] " + c.getDescripcion() + "\n");
        } catch (Exception ex) {
            error(ex);
        }
        cargar();
    }

    private void rehacer() {
        try {
            IComando c = gestor.rehacer();
            txtEventos.append("[REHACER] " + c.getDescripcion() + "\n");
        } catch (Exception ex) {
            error(ex);
        }
        cargar();
    }

    private void sincronizar() {
        List<String> r = gestor.sincronizar();
        txtEventos.append("[SINCRONIZAR] " + r.size() + " comando(s)\n");
        for (String s : r) {
            txtEventos.append("   " + s + "\n");
        }
        cargar();
    }

    private void actualizarComandos() {
        btnDeshacer.setEnabled(gestor.puedeDeshacer());
        btnRehacer.setEnabled(gestor.puedeRehacer());
        btnSincronizar.setEnabled(!gestor.getPendientes().isEmpty());
        lblComandos.setText("<html>Siguiente a deshacer: <b>" + gestor.getSiguienteDeshacer()
                + "</b> · Pendientes: " + gestor.getPendientes().size() + "</html>");
        mComandos.setRowCount(0);
        int i = 1;
        for (IComando c : gestor.getHistorial()) {
            mComandos.addRow(new Object[]{i++, c.getDescripcion(), c.getEstadoPrevio(), c.getEstadoNuevo()});
        }
    }

    // ============================================================ memento
    private void guardarPunto() {
        String cod = txtCodigo.getText().trim();
        if (cod.isEmpty()) {
            return;
        }
        String etiqueta = JOptionPane.showInputDialog(this, "Etiqueta del punto de restauración:",
                "Antes de reprogramar");
        if (etiqueta == null) {
            return;
        }
        try {
            caretaker.guardar(servicio.crearPuntoRestauracion(cod, etiqueta));
            cargar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void restaurarPunto() {
        EnvioMemento m = lstMementos.getSelectedValue();
        if (m == null) {
            info("Seleccione un punto de restauración.");
            return;
        }
        try {
            servicio.restaurar(m.getCodigoEnvio(), m, "punto \"" + m.getEtiqueta() + "\"");
            caretaker.descartar(m);
        } catch (Exception ex) {
            error(ex);
        }
        cargar();
    }

    // =========================================================== observer
    @Override
    public void actualizar(EventoEnvio e) {
        String linea = String.format("[%s] %s  %s -> %s  | %s | %s%s%n",
                e.getFechahora().format(HORA), e.getCodigo(), e.getEstadoAnterior(), e.getEstadoNuevo(),
                e.getUbicacion(), e.getObservacion(), e.isReversion() ? "  (REVERSIÓN)" : "");
        SwingUtilities.invokeLater(() -> txtEventos.append(linea));
    }

    @Override
    public String getNombre() {
        return "Pantalla de seguimiento";
    }
}
