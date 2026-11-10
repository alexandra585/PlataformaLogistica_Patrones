package uni.reportes;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;

/** ConcreteImplementor: envía el reporte a la impresora (muestra el diálogo del sistema). */
public class SalidaImpresora implements ISalidaReporte {

    @Override
    public String emitir(JasperPrint impreso, String titulo) throws Exception {
        boolean ok = JasperPrintManager.printReport(impreso, true);
        return ok ? "Reporte enviado a la impresora." : "Impresión cancelada.";
    }
}
