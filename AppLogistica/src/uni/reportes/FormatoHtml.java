package uni.reportes;

/**
 * ConcreteImplementor agregado en el Avance 2: tabla HTML que se muestra
 * en ReporteView (JEditorPane) o se guarda como archivo .html.
 * Demuestra el beneficio del Bridge: ningún reporte se modificó.
 */
public class FormatoHtml implements IFormatoReporte {

    private final StringBuilder sb = new StringBuilder();

    @Override
    public void iniciar(String titulo) {
        sb.setLength(0);
        sb.append("<html><body style='font-family:sans-serif'>")
          .append("<h2 style='color:#1F4E79'>").append(titulo).append("</h2>")
          .append("<table border='1' cellpadding='4' cellspacing='0'>");
    }

    @Override
    public void encabezado(String... columnas) {
        sb.append("<tr style='background:#1F4E79;color:white'>");
        for (String c : columnas) {
            sb.append("<th>").append(c).append("</th>");
        }
        sb.append("</tr>");
    }

    @Override
    public void fila(Object... valores) {
        sb.append("<tr>");
        for (Object v : valores) {
            sb.append("<td>").append(v).append("</td>");
        }
        sb.append("</tr>");
    }

    @Override
    public String finalizar() {
        return sb.append("</table><p style='font-size:9px'>LogiRuta Perú S.A.C. - AppLogistica</p>")
                 .append("</body></html>").toString();
    }
}
