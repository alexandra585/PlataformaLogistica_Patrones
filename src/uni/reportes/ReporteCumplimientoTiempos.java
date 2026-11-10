package uni.reportes;

/** RefinedAbstraction: % de envíos entregados dentro del plazo de cada servicio. */
public class ReporteCumplimientoTiempos extends Reporte {

    public ReporteCumplimientoTiempos(ISalidaReporte salida) {
        super(salida);
    }

    @Override
    public String getTitulo() {
        return "Cumplimiento de tiempos";
    }

    @Override
    protected String getPlantilla() {
        return "cumplimientoTiempos.jrxml";
    }
}
