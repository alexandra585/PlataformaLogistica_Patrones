package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import uni.controller.EmpleadoController;
import uni.dao.CatalogoDao;
import uni.entity.EmpleadoTo;
import uni.entity.ItemCombo;
import uni.entity.RolUsuario;

/** Mantenimiento de empleados / usuarios con rol (molde empleadomanView de AppEmpresa). */
public class EmpleadoView extends FormularioBase {

    private final EmpleadoController controller = new EmpleadoController();
    private final JTextField txtId = new JTextField(6);
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtApellidos = new JTextField(18);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtUsuario = new JTextField(12);
    private final JPasswordField txtClave = new JPasswordField(12);
    private final JComboBox<RolUsuario> cboRol = new JComboBox<>(new RolUsuario[]{
        RolUsuario.AGENTE_VENTANILLA, RolUsuario.OPERADOR_ALMACEN, RolUsuario.SUPERVISOR_RUTA,
        RolUsuario.REPARTIDOR, RolUsuario.ADMINISTRADOR});
    private final JComboBox<ItemCombo> cboSucursal = new JComboBox<>();
    private final DefaultTableModel modelo = modelo("Código", "Nombre", "Apellidos", "Email",
            "Usuario", "Rol", "Sucursal", "Activo", "Estado laboral", "Área", "Sueldo", "Régimen");
    private final JTable tbl = tabla(modelo);

    public EmpleadoView() {
        super("Mantenimiento de Empleados", 980, 600);
        txtId.setEditable(false);
        getContentPane().add(cabecera("Empleados y usuarios",
                "Rol = permisos del Proxy · Estado laboral = State + Observer (Sesión 12) · Nómina = Command + Memento (Sesión 13)"), BorderLayout.NORTH);
        try {
            for (ItemCombo i : new CatalogoDao().sucursales()) {
                cboSucursal.addItem(i);
            }
        } catch (Exception ex) {
            error(ex);
        }
        JPanel datos = formulario("Datos del empleado", "Código", txtId, "Nombre", txtNombre,
                "Apellidos", txtApellidos, "Email", txtEmail, "Usuario", txtUsuario,
                "Clave (vacío = no cambiar)", txtClave, "Rol", cboRol, "Sucursal", cboSucursal);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(boton("Nuevo", "new.png", e -> limpiar()));
        botones.add(boton("Grabar", "save_all.png", e -> grabar()));
        botones.add(boton("Dar de baja", "delete.png", e -> eliminar()));
        botones.add(boton("Estado laboral (State)", "update.png", e -> cambiarEstadoLaboral()));
        botones.add(boton("Historial laboral", "Information.png", e -> verHistorialLaboral()));
        botones.add(boton("Nómina (Command/Memento)", "Chart.png", e -> abrirNomina()));
        botones.add(boton("Boleta (GRASP)", "printer.png", e -> abrirInterno(new BoletaPagoView(txtId.getText().trim()))));
        botones.add(boton("Cerrar", "Cancel.png", e -> dispose()));
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(datos, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(norte, BorderLayout.NORTH);
        centro.add(new JScrollPane(tbl), BorderLayout.CENTER);
        getContentPane().add(centro, BorderLayout.CENTER);

        tbl.getSelectionModel().addListSelectionListener(e -> {
            int f = tbl.getSelectedRow();
            if (!e.getValueIsAdjusting() && f >= 0) {
                int r = tbl.convertRowIndexToModel(f);
                txtId.setText(String.valueOf(modelo.getValueAt(r, 0)));
                txtNombre.setText(String.valueOf(modelo.getValueAt(r, 1)));
                txtApellidos.setText(String.valueOf(modelo.getValueAt(r, 2)));
                txtEmail.setText(modelo.getValueAt(r, 3) == null ? "" : modelo.getValueAt(r, 3).toString());
                txtUsuario.setText(String.valueOf(modelo.getValueAt(r, 4)));
                cboRol.setSelectedItem(RolUsuario.valueOf(String.valueOf(modelo.getValueAt(r, 5))));
                String suc = String.valueOf(modelo.getValueAt(r, 6));
                for (int i = 0; i < cboSucursal.getItemCount(); i++) {
                    if (cboSucursal.getItemAt(i).getId().equals(suc)) {
                        cboSucursal.setSelectedIndex(i);
                    }
                }
                txtClave.setText("");
            }
        });
        listar();
    }

    private void listar() {
        modelo.setRowCount(0);
        try {
            for (EmpleadoTo e : controller.empleadoListar()) {
                modelo.addRow(new Object[]{e.getIdempleado(), e.getNombre(), e.getApellidos(), e.getEmail(),
                    e.getUsuario(), e.getRol(), e.getIdsucursal(), e.isActivo() ? "Sí" : "No", e.getEstadoLaboral(),
                    e.getArea(), String.format("%.2f", e.getSueldo()), e.getRegimen()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void limpiar() {
        tbl.clearSelection();
        for (JTextField t : new JTextField[]{txtId, txtNombre, txtApellidos, txtEmail, txtUsuario, txtClave}) {
            t.setText("");
        }
    }

    private void grabar() {
        boolean nuevo = txtId.getText().trim().isEmpty();
        EmpleadoTo e = new EmpleadoTo(txtId.getText().trim(), txtNombre.getText().trim(),
                txtApellidos.getText().trim(), (RolUsuario) cboRol.getSelectedItem());
        e.setEmail(txtEmail.getText().trim());
        e.setUsuario(txtUsuario.getText().trim());
        e.setClave(new String(txtClave.getPassword()));
        e.setIdsucursal(((ItemCombo) cboSucursal.getSelectedItem()).getId());
        try {
            controller.empleadoGrabar(e, nuevo);
            info("Empleado " + e.getIdempleado() + " grabado.");
            listar();
            limpiar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void eliminar() {
        String id = txtId.getText().trim();
        if (!id.isEmpty() && JOptionPane.showConfirmDialog(this, "¿Dar de baja a " + id + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                controller.empleadoEliminar(id);
                listar();
            } catch (Exception ex) {
                error(ex);
            }
        }
    }

    /** PATRÓN STATE + OBSERVER: el estado actual ofrece solo las transiciones válidas. */
    private void cambiarEstadoLaboral() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            info("Seleccione un empleado de la lista.");
            return;
        }
        try {
            uni.service.EmpleadoContexto ctx = uni.util.ConfiguracionSistema.getInstancia()
                    .getGestionLaboral().cargar(id);
            java.util.List<String> acciones = ctx.getEstado().accionesPermitidas();
            if (acciones.isEmpty()) {
                info(ctx.mostrarEstado());
                return;
            }
            Object accion = JOptionPane.showInputDialog(this, ctx.mostrarEstado() + "\n\nAcción a realizar:",
                    "Estado laboral", JOptionPane.QUESTION_MESSAGE, null, acciones.toArray(), acciones.get(0));
            if (accion == null) {
                return;
            }
            String motivo = JOptionPane.showInputDialog(this, "Motivo:", "");
            String resultado = controller.empleadoCambiarEstado(id, accion.toString(), motivo);
            info(resultado + "\nSe notificó a Recursos Humanos y a Operaciones (Observer).");
            listar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void verHistorialLaboral() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            info("Seleccione un empleado de la lista.");
            return;
        }
        try {
            StringBuilder sb = new StringBuilder("Historial laboral de " + id + ":\n");
            for (Object[] f : controller.historialLaboral(id)) {
                sb.append(String.format("%s  %s -> %s  %s (por %s)%n", f[0], f[1], f[2],
                        f[3] == null ? "" : f[3], f[4] == null ? "-" : f[4]));
            }
            info(sb.toString());
        } catch (Exception ex) {
            error(ex);
        }
    }

    /** Sesión 13: abre la nómina del empleado seleccionado (Command + Memento). */
    private void abrirNomina() {
        NominaEmpleadoView v = new NominaEmpleadoView(txtId.getText().trim());
        getDesktopPane().add(v);
        v.setLocation(Math.max(0, getX() + 30), Math.max(0, getY() + 30));
        v.setVisible(true);
        v.toFront();
        v.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                listar();
            }
        });
    }

    /** Sesión 17: abre la boleta de pago del empleado seleccionado. */
    private void abrirInterno(javax.swing.JInternalFrame v) {
        getDesktopPane().add(v);
        v.setLocation(Math.max(0, getX() + 30), Math.max(0, getY() + 30));
        v.setVisible(true);
        v.toFront();
    }
}
