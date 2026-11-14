package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.YearMonth;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import uni.controller.EmpleadoController;
import uni.entity.BoletaPagoTo;
import uni.entity.EmpleadoTo;
import uni.reportes.SalidaVisorJasper;
import uni.service.EmpleadoNomina;
import uni.service.GeneradorBoleta;
import uni.util.DescuentoFactory;

/**
 * Sesión 17 - Boleta de pago con patrones GRASP.
 *  - EXPERTO EN INFORMACIÓN: EmpleadoNomina calcula horas extras, descuentos y neto.
 *  - POLIMORFISMO: el régimen elegido (AFP / SNP) es un DescuentoStrategy.
 *  - FABRICACIÓN PURA: GeneradorBoleta arma, registra e imprime la boleta.
 * El formulario solo recoge datos y muestra resultados (Sesión 14: sin God Object).
 */
public class BoletaPagoView extends FormularioBase {

    private final EmpleadoController controller = new EmpleadoController();
    private final GeneradorBoleta generador = controller.boletaGenerador();
    private final JComboBox<String> cboEmpleado = new JComboBox<>();
    private final JTextField txtPeriodo = new JTextField(YearMonth.now().toString(), 7);
    private final JSpinner spnHoras = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));
    private final JComboBox<String> cboRegimen = new JComboBox<>(DescuentoFactory.codigos());
    private final JTextArea txtVista = new JTextArea();
    private final DefaultTableModel modelo = modelo("N.°", "Periodo", "Horas extra", "Régimen", "Ingresos",
            "Descuentos", "Neto", "Emitida");
    private final JTable tbl = tabla(modelo);
    private EmpleadoNomina empleado;

    public BoletaPagoView(String idInicial) {
        super("Boleta de pago - GRASP", 980, 600);
        getContentPane().add(cabecera("Boleta de pago (Sesión 17)",
                "Experto: EmpleadoNomina · Polimorfismo: DescuentoStrategy (AFP / SNP) · "
                + "Fabricación pura: GeneradorBoleta + BoletaDao"), BorderLayout.NORTH);

        JPanel datos = formulario("Datos de la boleta", "Empleado", cboEmpleado, "Periodo (AAAA-MM)", txtPeriodo,
                "Horas extras", spnHoras, "Régimen pensionario", cboRegimen);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(boton("Calcular", "Chart.png", e -> calcular()));
        botones.add(boton("Emitir boleta (PDF)", "printer.png", e -> emitir()));
        botones.add(boton("Ver boleta seleccionada", "Information.png", e -> verSeleccionada()));
        botones.add(boton("Cerrar", "Cancel.png", e -> dispose()));
        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.add(datos, BorderLayout.NORTH);
        izquierda.add(botones, BorderLayout.CENTER);

        txtVista.setEditable(false);
        txtVista.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane vista = new JScrollPane(txtVista);
        vista.setBorder(BorderFactory.createTitledBorder("Vista previa (calculada por el Experto)"));
        JSplitPane arriba = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierda, vista);
        arriba.setDividerLocation(470);

        JScrollPane historial = new JScrollPane(tbl);
        historial.setBorder(BorderFactory.createTitledBorder("Boletas emitidas (tabla boletas_pago)"));
        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, historial);
        centro.setDividerLocation(300);
        getContentPane().add(centro, BorderLayout.CENTER);
        getContentPane().add(new JLabel("  Fórmula del caso de clase: horas extras = sueldo x horas / 240; "
                + "AFP 11 %, SNP 6 % y salud 3 % sobre el sueldo básico."), BorderLayout.SOUTH);

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
        cboRegimen.addActionListener(e -> {
            if (empleado != null) {
                empleado.setRegimen(DescuentoFactory.crear(String.valueOf(cboRegimen.getSelectedItem())));
            }
        });
        cargar();
    }

    private void cargar() {
        if (cboEmpleado.getSelectedItem() == null) {
            return;
        }
        String id = String.valueOf(cboEmpleado.getSelectedItem()).substring(0, 5);
        try {
            empleado = controller.nominaCrear(id);
            cboRegimen.setSelectedItem(empleado.getRegimen().getCodigo());
            txtVista.setText("");
            listar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void calcular() {
        try {
            BoletaPagoTo b = generador.calcular(empleado, txtPeriodo.getText().trim(), (Integer) spnHoras.getValue());
            txtVista.setText(b.resumen() + "\n\n(Vista previa: aún no se ha registrado)");
            txtVista.setCaretPosition(0);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void emitir() {
        try {
            String usuario = uni.util.SesionUsuario.getInstancia().getUsuario().getIdempleado();
            BoletaPagoTo b = generador.emitir(empleado, txtPeriodo.getText().trim(), (Integer) spnHoras.getValue(),
                    usuario);
            txtVista.setText(b.resumen() + "\n\nBoleta N.° " + b.getIdboleta() + " registrada.");
            listar();
            generador.imprimir(b, visor());
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void verSeleccionada() {
        int f = tbl.getSelectedRow();
        if (f < 0) {
            info("Seleccione una boleta de la lista.");
            return;
        }
        BoletaPagoTo b = new BoletaPagoTo();
        b.setIdboleta((Integer) modelo.getValueAt(tbl.convertRowIndexToModel(f), 0));
        try {
            generador.imprimir(b, visor());
        } catch (Exception ex) {
            error(ex);
        }
    }

    private SalidaVisorJasper visor() {
        return new SalidaVisorJasper(frame -> {
            getDesktopPane().add(frame);
            frame.setLocation(Math.max(0, getX() + 20), Math.max(0, getY() + 20));
            frame.setVisible(true);
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException ignorar) {
                // sin efecto
            }
        });
    }

    private void listar() {
        modelo.setRowCount(0);
        try {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Object[] f : controller.boletasListar(empleado.getIdempleado())) {
                f[7] = fmt.format(f[7]);
                modelo.addRow(f);
            }
        } catch (Exception ex) {
            error(ex);
        }
    }
}
