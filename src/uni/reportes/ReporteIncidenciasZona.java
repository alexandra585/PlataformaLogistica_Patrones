package uni.reportes;

/** RefinedAbstraction: Incidencias de reparto agrupadas por zona. */
public class ReporteIncidenciasZona extends Reporte {

    public ReporteIncidenciasZona(ISalidaReporte salida) {
        super(salida);
    }

    @Override
    public String getTitulo() {
        return "Incidencias por zona";
    }

    @Override
    protected String getPlantilla() {
        return "incidenciasZona.jrxml";
    }
}
