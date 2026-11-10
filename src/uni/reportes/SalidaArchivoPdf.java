package uni.reportes;

import java.io.File;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

/** ConcreteImplementor: exporta el reporte a un archivo PDF. */
public class SalidaArchivoPdf implements ISalidaReporte {

    private final File destino;

    public SalidaArchivoPdf(File destino) {
        this.destino = destino;
    }

    @Override
    public String emitir(JasperPrint impreso, String titulo) throws Exception {
        File padre = destino.getAbsoluteFile().getParentFile();
        if (padre != null && !padre.exists()) {
            padre.mkdirs();
        }
        JasperExportManager.exportReportToPdfFile(impreso, destino.getAbsolutePath());
        return "PDF generado (" + impreso.getPages().size() + " pág.): " + destino.getAbsolutePath();
    }
}
