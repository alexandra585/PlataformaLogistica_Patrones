package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import uni.dao.CatalogoDao;
import uni.entity.ItemCombo;

public class ClienteBuscarDialog extends JDialog {

    private JTextField txtFiltro = new JTextField(20);
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private JButton btnBuscar = new JButton("Buscar");
    private JButton btnSeleccionar = new JButton("Seleccionar");
    private JButton btnCancelar = new JButton("Cancelar");
    
    private ItemCombo clienteSeleccionado = null;
    private List<ItemCombo> listaClientesOriginal;

    public ClienteBuscarDialog(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Buscar y Seleccionar Remitente");
        setSize(500, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        // Panel superior para filtrar / buscar
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelNorte.add(new JLabel("Filtrar por nombre/código:"));
        panelNorte.add(txtFiltro);
        panelNorte.add(btnBuscar);
        add(panelNorte, BorderLayout.NORTH);

        // Tabla central de clientes
        modeloTabla = new DefaultTableModel(new Object[]{"Código", "Nombre del Cliente"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Evita que editen las celdas directamente
            }
        };
        tablaClientes = new JTable(modeloTabla);
        add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        // Panel inferior de botones
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.add(btnSeleccionar);
        panelSur.add(btnCancelar);
        add(panelSur, BorderLayout.SOUTH);

        // Cargar datos iniciales
        cargarDatos("");

        // Eventos
        btnBuscar.addActionListener(e -> filtrarDatos(txtFiltro.getText().trim()));
        txtFiltro.addActionListener(e -> filtrarDatos(txtFiltro.getText().trim())); // Enter en el buscador
        
        btnSeleccionar.addActionListener(e -> seleccionarCliente());
        btnCancelar.addActionListener(e -> dispose());

        // Doble clic en la tabla también selecciona el cliente automáticamente
        tablaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    seleccionarCliente();
                }
            }
        });
    }

    private void cargarDatos(String filtro) {
        try {
            CatalogoDao cat = new CatalogoDao();
            listaClientesOriginal = cat.clientes(); // Obtiene la lista de clientes desde la BD
            modeloTabla.setRowCount(0);
            
            for (ItemCombo c : listaClientesOriginal) {
                if (filtro.isEmpty() || c.toString().toLowerCase().contains(filtro.toLowerCase())) {
                    modeloTabla.addRow(new Object[]{c.getId(), c.toString()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarDatos(String texto) {
        modeloTabla.setRowCount(0);
        for (ItemCombo c : listaClientesOriginal) {
            if (c.toString().toLowerCase().contains(texto.toLowerCase()) || c.getId().toLowerCase().contains(texto.toLowerCase())) {
                modeloTabla.addRow(new Object[]{c.getId(), c.toString()});
            }
        }
    }

    private void seleccionarCliente() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un cliente de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String codigo = (String) modeloTabla.getValueAt(fila, 0);
        String descripcion = (String) modeloTabla.getValueAt(fila, 1);
        
        // Creamos el ItemCombo con la selección
        clienteSeleccionado = new ItemCombo(codigo, descripcion);
        dispose(); // Cierra la ventana modal
    }

    public ItemCombo getClienteSeleccionado() {
        return clienteSeleccionado;
    }
}
