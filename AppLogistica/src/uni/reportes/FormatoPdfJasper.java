package uni.reportes;

import java.util.ArrayList;
import java.util.List;

/**
 * ConcreteImplementor: PDF con JasperReports (como listaEmpleados.jrxml de
 * AppEmpresa). En la demo se simula el llenado del datasource.
 */
public class FormatoPdfJasper implements IFormatoReporte {

    private String plantilla;
    private final List<Object[]> datasource = new ArrayList<>();

    @Override
    public void iniciar(String titulo) {
        plantilla = titulo.toLowerCase().replace(' ', '_') + ".jasper";
        datasource.clear();
    }

    @Override
    public void encabezado(String... columnas) {
        // Las columnas están definidas en el diseño .jrxml
    }

    @Override
    public void fila(Object... valores) {
        datasource.add(valores);
    }

    @Override
    public String finalizar() {
        // Real: JasperFillManager.fillReport(plantilla, params, new JRBeanArrayDataSource(...))
        //       JasperExportManager.exportReportToPdfFile(print, ruta);
        return "PDF generado con plantilla " + plantilla + " (" + datasource.size() + " filas)";
    }
}
