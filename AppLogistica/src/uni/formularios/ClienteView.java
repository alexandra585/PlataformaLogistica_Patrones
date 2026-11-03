package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import uni.controller.ClienteController;
import uni.entity.ClienteTo;

/** Mantenimiento de clientes naturales y corporativos. */
public class ClienteView extends FormularioBase {

    private final ClienteController controller = new ClienteController();
    private final JTextField txtId = new JTextField(6);
    private final JTextField txtRazon = new JTextField(28);
    private final JComboBox<String> cboTipo = new JComboBox<>(new String[]{"NATURAL", "CORPORATIVO"});
    private final JTextField txtDoc = new JTextField(12);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtTel = new JTextField(12);
    private final DefaultTableModel modelo = modelo("Código", "Nombre / Razón social", "Tipo",
            "DNI / RUC", "Email", "Teléfono");
    private final JTable tbl = tabla(modelo);

    public ClienteView() {
        super("Mantenimiento de Clientes", 820, 520);
        txtId.setEditable(false);
        getContentPane().add(cabecera("Clientes", "Remitentes naturales y corporativos"), BorderLayout.NORTH);

        JPanel datos = formulario("Datos del cliente", "Código", txtId, "Nombre / Razón social", txtRazon,
                "Tipo", cboTipo, "DNI / RUC", txtDoc, "Email", txtEmail, "Teléfono", txtTel);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(boton("Nuevo", "new.png", e -> limpiar()));
        botones.add(boton("Grabar", "save_all.png", e -> grabar()));
        botones.add(boton("Eliminar", "delete.png", e -> eliminar()));
        JButton cerrar = boton("Cerrar", "Cancel.png", e -> dispose());
        botones.add(cerrar);

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
                txtRazon.setText(String.valueOf(modelo.getValueAt(r, 1)));
                cboTipo.setSelectedItem(modelo.getValueAt(r, 2));
                txtDoc.setText(String.valueOf(modelo.getValueAt(r, 3)));
                txtEmail.setText(valor(modelo.getValueAt(r, 4)));
                txtTel.setText(valor(modelo.getValueAt(r, 5)));
            }
        });
        listar();
    }

    private static String valor(Object o) {
        return o == null ? "" : o.toString();
    }

    private void listar() {
        modelo.setRowCount(0);
        try {
            for (ClienteTo c : controller.clienteListar()) {
                modelo.addRow(new Object[]{c.getIdcliente(), c.getRazonsocial(), c.getTipocliente(),
                    c.getNrodocumento(), c.getEmail(), c.getTelefono()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void limpiar() {
        tbl.clearSelection();
        for (JTextField t : new JTextField[]{txtId, txtRazon, txtDoc, txtEmail, txtTel}) {
            t.setText("");
        }
        txtRazon.requestFocus();
    }

    private void grabar() {
        ClienteTo c = new ClienteTo(txtId.getText().trim(), txtRazon.getText().trim(),
                (String) cboTipo.getSelectedItem(), txtDoc.getText().trim(),
                txtEmail.getText().trim(), txtTel.getText().trim());
        try {
            controller.clienteGrabar(c);
            info("Cliente " + c.getIdcliente() + " grabado correctamente.");
            listar();
            limpiar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void eliminar() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            info("Seleccione un cliente de la lista.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el cliente " + id + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                controller.clienteEliminar(id);
                listar();
                limpiar();
            } catch (Exception ex) {
                error(ex);
            }
        }
    }
}
