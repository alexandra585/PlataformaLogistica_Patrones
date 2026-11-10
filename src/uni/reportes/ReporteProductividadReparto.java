package uni.reportes;

/** RefinedAbstraction: Entregas y observaciones por repartidor. */
public class ReporteProductividadReparto extends Reporte {

    public ReporteProductividadReparto(ISalidaReporte salida) {
        super(salida);
    }

    @Override
    public String getTitulo() {
        return "Productividad de reparto";
    }

    @Override
    protected String getPlantilla() {
        return "productividadReparto.jrxml";
    }
}
