package uni.reportes;

import net.sf.jasperreports.engine.JasperPrint;

/**
 * PATRÓN BRIDGE - Implementor.
 * Define CÓMO se entrega un reporte ya llenado por JasperReports
 * (visor en pantalla, archivo PDF o impresora), independiente de
 * QUÉ información contiene.
 */
public interface ISalidaReporte {

    /** @return mensaje para el usuario (ruta del PDF, "enviado a impresora", etc.). */
    String emitir(JasperPrint impreso, String titulo) throws Exception;
}
