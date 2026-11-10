package uni.formularios;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import uni.dao.ManifiestoDao;
import uni.reportes.ISalidaReporte;
import uni.reportes.Reporte;
import uni.reportes.ReporteCumplimientoTiempos;
import uni.reportes.ReporteIncidenciasZona;
import uni.reportes.ReporteListaEmpleados;
import uni.reportes.ReporteListadoEnvios;
import uni.reportes.ReporteManifiesto;
import uni.reportes.ReporteProductividadReparto;
import uni.reportes.SalidaArchivoPdf;
import uni.reportes.SalidaImpresora;
import uni.reportes.SalidaVisorJasper;

/**
 * Reportes con JasperReports (como InformeView de AppEmpresa).
 * PATRÓN BRIDGE: el usuario combina QUÉ reporte (Reporte, abstracción)
 * con CÓMO se entrega (ISalidaReporte: visor, PDF o impresora).
 */
public class ReporteView extends FormularioBase {

    private static final String[] REPORTES = {
        "Lista de empleados", "Listado de envíos", "Manifiesto de carga",
        "Cumplimiento de tiempos", "Incidencias por zona", "Productividad de reparto"};
    private static final String[] DESCRIPCIONES = {
        "Personal activo con rol, sucursal, área, estado laboral y sueldo; total de la planilla.",
        "Envíos agrupados por estado, con cliente, servicio, destino, peso y costo. Puede filtrarse por estado.",
        "Detalle de un manifiesto: recorre el árbol Composite (manifiesto > paletas/sacos > paquetes).",
        "Porcentaje de envíos entregados dentro del plazo de cada tipo de servicio.",
        "Incidencias de reparto agrupadas por zona, con motivo y responsable.",
        "Entregas y observaciones por repartidor, con su efectividad."};

    private final JComboBox<String> cboReporte = new JComboBox<>(REPORTES);
    private final JComboBox<String> cboSalida = new JComboBox<>(new String[]{
        "Visor JasperReports", "Archivo PDF", "Impresora"});
    private final JComboBox<String> cboEstado = new JComboBox<>(new String[]{
        "(Todos)", "REGISTRADO", "RECOGIDO", "EN_TRANSITO", "CLASIFICADO", "EN_RUTA",
        "OBSERVADO", "REPROGRAMADO", "ENTREGADO", "DEVUELTO"});
    private final JComboBox<String> cboManifiesto = new JComboBox<>();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel filtros = new JPanel(tarjetas);
    private final JTextArea txtDescripcion = new JTextArea(3, 40);

    public ReporteView() {
        super("Reportes (JasperReports)", 760, 330);
        getContentPane().add(cabecera("Reportes para la toma de decisiones",
                "JasperReports · Bridge: Reporte (qué) × ISalidaReporte (visor / PDF / impresora)"), BorderLayout.NORTH);

        JPanel pEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pEstado.add(new JLabel("Estado:"));
        pEstado.add(cboEstado);
        JPanel pManifiesto = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pManifiesto.add(new JLabel("Manifiesto:"));
        pManifiesto.add(cboManifiesto);
        filtros.add(new JPanel(), "ninguno");
        filtros.add(pEstado, "estado");
        filtros.add(pManifiesto, "manifiesto");
        try {
            for (String m : new ManifiestoDao().listarCodigos()) {
                cboManifiesto.addItem(m);
            }
        } catch (Exception ex) {
            error(ex);
        }

        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setOpaque(false);
        txtDescripcion.setBorder(BorderFactory.createTitledBorder("Descripción"));

        JPanel datos = formulario("Opciones del reporte", "Reporte", cboReporte, "Filtro", filtros,
                "Salida", cboSalida);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(boton("Generar", "printer.png", e -> generar()));
        botones.add(boton("Cerrar", "Cancel.png", e -> dispose()));
        JPanel centro = new JPanel(new BorderLayout(6, 6));
        centro.add(datos, BorderLayout.NORTH);
        centro.add(txtDescripcion, BorderLayout.CENTER);
        centro.add(botones, BorderLayout.SOUTH);
        getContentPane().add(centro, BorderLayout.CENTER);

        cboReporte.addActionListener(e -> actualizarFiltros());
        actualizarFiltros();
    }

    private void actualizarFiltros() {
        int i = cboReporte.getSelectedIndex();
        tarjetas.show(filtros, i == 1 ? "estado" : i == 2 ? "manifiesto" : "ninguno");
        txtDescripcion.setText(DESCRIPCIONES[i]);
    }

    /** Implementor elegido por el usuario; null si canceló el diálogo de guardar. */
    private ISalidaReporte salidaElegida() {
        switch (cboSalida.getSelectedIndex()) {
            case 1:
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Documento PDF", "pdf"));
                fc.setSelectedFile(new File(String.valueOf(cboReporte.getSelectedItem()).replace(' ', '_') + ".pdf"));
                if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                    return null;
                }
                File f = fc.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".pdf")) {
                    f = new File(f.getParentFile(), f.getName() + ".pdf");
                }
                return new SalidaArchivoPdf(f);
            case 2:
                return new SalidaImpresora();
            default:
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
    }

    private Reporte reporteElegido(ISalidaReporte salida) {
        switch (cboReporte.getSelectedIndex()) {
            case 1:
                String estado = cboEstado.getSelectedIndex() == 0 ? null : String.valueOf(cboEstado.getSelectedItem());
                return new ReporteListadoEnvios(salida, estado);
            case 2:
                if (cboManifiesto.getSelectedItem() == null) {
                    throw new IllegalStateException("Aún no hay manifiestos registrados.");
                }
                return new ReporteManifiesto(salida, String.valueOf(cboManifiesto.getSelectedItem()));
            case 3:
                return new ReporteCumplimientoTiempos(salida);
            case 4:
                return new ReporteIncidenciasZona(salida);
            case 5:
                return new ReporteProductividadReparto(salida);
            default:
                return new ReporteListaEmpleados(salida);
        }
    }

    private void generar() {
        try {
            ISalidaReporte salida = salidaElegida();
            if (salida == null) {
                return;
            }
            String r = reporteElegido(salida).generar();
            if (!(salida instanceof SalidaVisorJasper)) {
                info(r);
            }
        } catch (Exception ex) {
            error(ex);
        }
    }
}
