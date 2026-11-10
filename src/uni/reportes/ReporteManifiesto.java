package uni.reportes;

import java.util.HashMap;
import java.util.Map;

/**
 * RefinedAbstraction: detalle de un manifiesto de carga. El .jrxml recorre el
 * árbol del Composite (manifiesto > paletas/sacos > paquetes) con una CTE recursiva.
 */
public class ReporteManifiesto extends Reporte {

    private final String idmanifiesto;

    public ReporteManifiesto(ISalidaReporte salida, String idmanifiesto) {
        super(salida);
        this.idmanifiesto = idmanifiesto;
    }

    @Override
    public String getTitulo() {
        return "Manifiesto " + idmanifiesto;
    }

    @Override
    protected String getPlantilla() {
        return "manifiestoCarga.jrxml";
    }

    @Override
    protected Map<String, Object> getParametros() {
        Map<String, Object> p = new HashMap<>();
        p.put("P_MANIFIESTO", idmanifiesto);
        return p;
    }
}
