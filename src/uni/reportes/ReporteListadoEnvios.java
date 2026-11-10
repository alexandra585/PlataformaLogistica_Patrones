package uni.reportes;

import java.util.HashMap;
import java.util.Map;

/** RefinedAbstraction: envíos agrupados por estado; filtro opcional por estado. */
public class ReporteListadoEnvios extends Reporte {

    private final String estado;

    /** @param estado estado a filtrar, o null para todos. */
    public ReporteListadoEnvios(ISalidaReporte salida, String estado) {
        super(salida);
        this.estado = estado;
    }

    @Override
    public String getTitulo() {
        return "Listado de envíos" + (estado == null ? "" : " - " + estado);
    }

    @Override
    protected String getPlantilla() {
        return "listadoEnvios.jrxml";
    }

    @Override
    protected Map<String, Object> getParametros() {
        Map<String, Object> p = new HashMap<>();
        p.put("P_ESTADO", estado);
        return p;
    }
}
