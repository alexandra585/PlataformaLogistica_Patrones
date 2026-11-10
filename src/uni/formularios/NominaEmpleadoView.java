package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import uni.controller.EmpleadoController;
import uni.controller.MenuComandos;
import uni.entity.EmpleadoTo;
import uni.service.AumentarSueldoCommand;
import uni.service.CalcularBonoCommand;
import uni.service.CalcularSueldoCommand;
import uni.service.CambiarAreaCommand;
import uni.service.EmpleadoMemento;
import uni.service.EmpleadoNomina;
import uni.service.IComandoEmpleado;
import uni.service.MostrarDatosCommand;
import uni.util.HistorialEmpleado;

/**
 * Sesión 13 - Command + Memento aplicados al empleado (como el ejemplo de clase).
 *  - Cada botón crea un comando concreto y lo entrega al Invoker (MenuComandos).
 *  - EmpleadoNomina es el Receiver y también el Originator.
 *  - HistorialEmpleado es el Caretaker: guarda los puntos de restauración.
 */
public class NominaEmpleadoView extends FormularioBase {

    /** Un Caretaker por empleado; vive mientras dure la sesión. */
    private static final Map<String, HistorialEmpleado> HISTORIALES = new HashMap<>();

    private final EmpleadoController controller = new EmpleadoController();
    private final MenuComandos menu = new MenuComandos();               // Invoker
    private EmpleadoNomina empleado;                                    // Receiver / Originator
    private HistorialEmpleado historial;                                // Caretaker

    private final JComboBox<String> cboEmpleado = new JComboBox<>();
    private final JLabel lblDatos = new JLabel(" ");
    private final JTextField txtPorcentaje = new JTextField("10", 5);
    private final JComboBox<String> cboArea = new JComboBox<>(new String[]{
        "OPERACIONES", "REPARTO", "ALMACEN", "ATENCION AL CLIENTE", "SUPERVISION", "GERENCIA"});
    private final DefaultListModel<EmpleadoMemento> puntos = new DefaultListModel<>();
    private final JList<EmpleadoMemento> lstPuntos = new JList<>(puntos);
    private final JTextArea txtBitacora = new JTextArea();
    private final DefaultTableModel modeloBd = modelo("Fecha", "Operación", "Sueldo anterior", "Sueldo nuevo",
            "Área anterior", "Área nueva", "Usuario");

    public NominaEmpleadoView(String idInicial) {
        super("Nómina del empleado - Command y Memento", 1180, 640);
        getContentPane().add(cabecera("Nómina del empleado (Sesión 13)",
                "Command: MenuComandos (Invoker) → comandos → EmpleadoNomina (Receiver)  ·  "
                + "Memento: EmpleadoNomina (Originator) / HistorialEmpleado (Caretaker)"), BorderLayout.NORTH);
        cboArea.setEditable(true);
        lblDatos.setFont(lblDatos.getFont().deriveFont(Font.BOLD, 13f));

        JPanel sel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sel.add(new JLabel("Empleado:"));
        sel.add(cboEmpleado);
        sel.add(lblDatos);

        JPanel comandos = new JPanel(new GridLayout(0, 1, 4, 4));
        comandos.setBorder(BorderFactory.createTitledBorder("Comandos (Invoker: MenuComandos)"));
        comandos.add(boton("Mostrar datos", "Information.png",
                e -> ejecutar(new MostrarDatosCommand(empleado))));
        comandos.add(boton("Calcular bono (10 %)", "barchart.png",
                e -> ejecutar(new CalcularBonoCommand(empleado))));
        comandos.add(boton("Calcular sueldo final", "Chart.png",
                e -> ejecutar(new CalcularSueldoCommand(empleado))));
        JPanel pAumento = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pAumento.add(new JLabel("%"));
        pAumento.add(txtPorcentaje);
        pAumento.add(boton("Aumentar sueldo", "update.png", e -> aumentar()));
        comandos.add(pAumento);
        JPanel pArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pArea.add(cboArea);
        pArea.add(boton("Cambiar área", "edit.png",
                e -> ejecutar(new CambiarAreaCommand(empleado, String.valueOf(cboArea.getSelectedItem()), historial))));
        comandos.add(pArea);

        JPanel memento = new JPanel(new BorderLayout(4, 4));
        memento.setBorder(BorderFactory.createTitledBorder("Puntos de restauración (Caretaker: HistorialEmpleado)"));
        memento.add(new JScrollPane(lstPuntos), BorderLayout.CENTER);
        JPanel bm = new JPanel(new GridLayout(0, 2, 4, 4));
        bm.add(boton("Guardar estado", "save_all.png", e -> guardarPunto()));
        bm.add(boton("Deshacer último", "go-first.png", e -> deshacer()));
        bm.add(boton("Restaurar seleccionado", "update.png", e -> restaurarSeleccionado()));
        memento.add(bm, BorderLayout.SOUTH);

        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.add(comandos, BorderLayout.NORTH);
        izquierda.add(memento, BorderLayout.CENTER);

        txtBitacora.setEditable(false);
        txtBitacora.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bitácora del Invoker", new JScrollPane(txtBitacora));
        tabs.addTab("Historial en BD (historial_remuneracion)", new JScrollPane(new JTable(modeloBd)));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierda, tabs);
        split.setDividerLocation(500);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(sel, BorderLayout.NORTH);
        centro.add(split, BorderLayout.CENTER);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.add(boton("Cerrar", "Cancel.png", e -> dispose()));
        centro.add(sur, BorderLayout.SOUTH);
        getContentPane().add(centro, BorderLayout.CENTER);

        try {
            for (EmpleadoTo e : controller.empleadoListar()) {
                cboEmpleado.addItem(e.getIdempleado() + " - " + e.getNombreCompleto());
                if (e.getIdempleado().equals(idInicial)) {
                    cboEmpleado.setSelectedIndex(cboEmpleado.getItemCount() - 1);
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
        cboEmpleado.addActionListener(e -> cargar());
        cargar();
    }

    private void cargar() {
        if (cboEmpleado.getSelectedItem() == null) {
            return;
        }
        String id = String.valueOf(cboEmpleado.getSelectedItem()).substring(0, 5);
        try {
            empleado = controller.nominaCrear(id);
            historial = HISTORIALES.computeIfAbsent(id, k -> new HistorialEmpleado());
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    /** El formulario solo arma el comando; el Invoker lo ejecuta. */
    private void ejecutar(IComandoEmpleado comando) {
        try {
            menu.setComando(comando);
            String r = menu.ejecutarComando();
            java.util.List<String> b = menu.getBitacora();
            txtBitacora.append(b.get(b.size() - 1) + "\n");
            refrescar();
            info(r);
        } catch (Exception ex) {
            error(ex);
            refrescar();
        }
    }

    private void aumentar() {
        double pct;
        try {
            pct = Double.parseDouble(txtPorcentaje.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            info("Ingrese un porcentaje numérico, por ejemplo 10.");
            return;
        }
        ejecutar(new AumentarSueldoCommand(empleado, pct, historial));
    }

    private void guardarPunto() {
        EmpleadoMemento m = empleado.guardar("Punto manual");
        historial.guardar(m);
        txtBitacora.append("[Memento] Guardado: " + m + "\n");
        refrescar();
    }

    private void deshacer() {
        EmpleadoMemento m = historial.deshacer();
        if (m == null) {
            info("No hay estados guardados para " + empleado.getIdempleado() + ".");
            return;
        }
        restaurar(m);
    }

    private void restaurarSeleccionado() {
        EmpleadoMemento m = lstPuntos.getSelectedValue();
        if (m == null) {
            info("Seleccione un punto de restauración.");
            return;
        }
        historial.descartar(m);
        restaurar(m);
    }

    private void restaurar(EmpleadoMemento m) {
        try {
            empleado.restaurar(m);
            txtBitacora.append("[Memento] Restaurado: " + m + "\n");
            refrescar();
            info("Estado restaurado: S/ " + String.format("%.2f", empleado.getSueldo()) + " - " + empleado.getArea());
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void refrescar() {
        lblDatos.setText(String.format("   Área: %s   |   Sueldo: S/ %.2f", empleado.getArea(), empleado.getSueldo()));
        puntos.clear();
        for (EmpleadoMemento m : historial.listar()) {
            puntos.addElement(m);
        }
        modeloBd.setRowCount(0);
        try {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM HH:mm:ss");
            for (Object[] f : controller.historialRemuneracion(empleado.getIdempleado())) {
                f[0] = fmt.format(f[0]);
                modeloBd.addRow(f);
            }
        } catch (Exception ex) {
            error(ex);
        }
    }
}
