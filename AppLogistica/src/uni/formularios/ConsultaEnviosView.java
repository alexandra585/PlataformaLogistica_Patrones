package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import uni.controller.EnvioController;
import uni.dao.EnvioDao;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;

/** Consulta de envíos por estado (doble clic abre el seguimiento). */
public class ConsultaEnviosView extends FormularioBase {

    private final EnvioController controller = new EnvioController();
    private final EnvioDao dao = new EnvioDao();
    private final JComboBox<Object> cboEstado = new JComboBox<>();
    private final DefaultTableModel modelo = modelo("Código", "Cliente", "Servicio", "Destinatario",
            "Distrito", "Estado", "Intentos", "Costo S/", "Entrega estimada");
    private final JTable tbl = tabla(modelo);
    private final JLabel lblTotal = new JLabel();

    public ConsultaEnviosView() {
        super("Consulta de envíos", 980, 520);
        getContentPane().add(cabecera("Envíos registrados", "Doble clic sobre un envío para ver su seguimiento"),
                BorderLayout.NORTH);
        cboEstado.addItem("TODOS");
        for (EstadoEnvio e : EstadoEnvio.values()) {
            cboEstado.addItem(e);
        }
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Estado:"));
        filtro.add(cboEstado);
        filtro.add(boton("Consultar", "buscar.gif", e -> consultar()));
        filtro.add(boton("Cerrar", "Cancel.png", e -> dispose()));
        filtro.add(lblTotal);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(filtro, BorderLayout.NORTH);
        centro.add(new JScrollPane(tbl), BorderLayout.CENTER);
        getContentPane().add(centro, BorderLayout.CENTER);
        tbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int f = tbl.getSelectedRow();
                if (e.getClickCount() == 2 && f >= 0) {
                    String cod = String.valueOf(modelo.getValueAt(tbl.convertRowIndexToModel(f), 0));
                    PrincipalView.getActual().abrir(new SeguimientoView(cod));
                }
            }
        });
        consultar();
    }

    private void consultar() {
        modelo.setRowCount(0);
        try {
            Object sel = cboEstado.getSelectedItem();
            List<EnvioTo> lista = (sel instanceof EstadoEnvio)
                    ? dao.listarPorEstado((EstadoEnvio) sel) : controller.envioListar();
            for (EnvioTo e : lista) {
                modelo.addRow(new Object[]{e.getCodigo(), e.getIdcliente(), e.getServicio(),
                    e.getNombreDestinatario(), e.getDestino() == null ? "Recojo " + e.getIdtiendaRecojo()
                    : e.getDestino().getDistrito(), e.getEstado(), e.getIntentos(),
                    String.format("%.2f", e.getCosto()), e.getFechaEstimada()});
            }
            lblTotal.setText("   " + lista.size() + " envío(s)");
        } catch (Exception ex) {
            error(ex);
        }
    }
}
