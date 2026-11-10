package uni.reportes;

import java.util.function.Consumer;
import javax.swing.JInternalFrame;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

/**
 * ConcreteImplementor: visor de JasperReports dentro del escritorio, igual que
 * InformeView de AppEmpresa. El JRViewer incluye el botón "Guardar" que
 * exporta a PDF.
 */
public class SalidaVisorJasper implements ISalidaReporte {

    private final Consumer<JInternalFrame> escritorio;

    /** @param escritorio cómo agregar la ventana al JDesktopPane (lo decide el formulario). */
    public SalidaVisorJasper(Consumer<JInternalFrame> escritorio) {
        this.escritorio = escritorio;
    }

    @Override
    public String emitir(JasperPrint impreso, String titulo) {
        JInternalFrame f = new JInternalFrame("Reporte - " + titulo, true, true, true, true);
        f.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        f.getContentPane().add(new JRViewer(impreso));
        f.setSize(900, 640);
        escritorio.accept(f);
        return "Reporte abierto en el visor de JasperReports.";
    }
}
