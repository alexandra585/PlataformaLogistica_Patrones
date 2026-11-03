package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import uni.dao.IncidenciaDao;
import uni.dao.ReporteDao;
import uni.reportes.FormatoCsv;
import uni.reportes.FormatoHtml;
import uni.reportes.FormatoPantalla;
import uni.reportes.IFormatoReporte;
import uni.reportes.Reporte;
import uni.reportes.ReporteCumplimientoTiempos;
import uni.reportes.ReporteIncidenciasZona;
import uni.reportes.ReporteProductividadReparto;

/**
 * Reportes estratégicos. PATRÓN BRIDGE: el usuario combina libremente
 * QUÉ reporte (abstracción) con CÓMO se presenta (implementador).
 */
public class ReporteView extends FormularioBase {

    private final JComboBox<String> cboReporte = new JComboBox<>(new String[]{
        "Incidencias por zona", "Cumplimiento de tiempos", "Productividad de reparto"});
    private final JComboBox<String> cboFormato = new JComboBox<>(new String[]{"Pantalla (texto)", "HTML", "CSV (Excel)"});
    private final JEditorPane visor = new JEditorPane();
    private String ultimoResultado = "";

    public ReporteView() {
        super("Reportes estratégicos", 860, 540);
        getContentPane().add(cabecera("Reportes para la toma de decisiones",
                "Bridge: Reporte (qué) × IFormatoReporte (cómo)"), BorderLayout.NORTH);
        JPanel opciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        opciones.add(new JLabel("Reporte:"));
        opciones.add(cboReporte);
        opciones.add(new JLabel("Formato:"));
        opciones.add(cboFormato);
        opciones.add(boton("Generar", "barchart.png", e -> generar()));
        opciones.add(boton("Exportar a archivo", "printer.png", e -> exportar()));
        opciones.add(boton("Cerrar", "Cancel.png", e -> dispose()));
        visor.setEditable(false);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(opciones, BorderLayout.NORTH);
        centro.add(new JScrollPane(visor), BorderLayout.CENTER);
        getContentPane().add(centro, BorderLayout.CENTER);
        generar();
    }

    private IFormatoReporte formatoElegido() {
        switch (cboFormato.getSelectedIndex()) {
            case 1:  return new FormatoHtml();
            case 2:  return new FormatoCsv();
            default: return new FormatoPantalla();
        }
    }

    private void generar() {
        try {
            IFormatoReporte formato = formatoElegido();
            Reporte r;
            switch (cboReporte.getSelectedIndex()) {
                case 1:
                    r = new ReporteCumplimientoTiempos(formato, new ReporteDao().cumplimientoTiempos());
                    break;
                case 2:
                    r = new ReporteProductividadReparto(formato, new ReporteDao().productividadReparto());
                    break;
                default:
                    r = new ReporteIncidenciasZona(formato, new IncidenciaDao().listarTodas());
            }
            ultimoResultado = r.generar();
            if (formato instanceof FormatoHtml) {
                visor.setContentType("text/html");
            } else {
                visor.setContentType("text/plain");
                visor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            }
            visor.setText(ultimoResultado);
            visor.setCaretPosition(0);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void exportar() {
        String ext = cboFormato.getSelectedIndex() == 1 ? ".html" : cboFormato.getSelectedIndex() == 2 ? ".csv" : ".txt";
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(String.valueOf(cboReporte.getSelectedItem()).replace(' ', '_') + ext));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(fc.getSelectedFile().toPath(), ultimoResultado.getBytes(StandardCharsets.UTF_8));
                info("Archivo guardado: " + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                error(ex);
            }
        }
    }
}
