package uni.reportes;

import java.util.HashMap;
import java.util.Map;

/** RefinedAbstraction: boleta de pago de un empleado (Sesión 17, GRASP). */
public class ReporteBoletaPago extends Reporte {

    private final int idboleta;

    public ReporteBoletaPago(ISalidaReporte salida, int idboleta) {
        super(salida);
        this.idboleta = idboleta;
    }

    @Override
    public String getTitulo() {
        return "Boleta de pago N.° " + idboleta;
    }

    @Override
    protected String getPlantilla() {
        return "boletaPago.jrxml";
    }

    @Override
    protected Map<String, Object> getParametros() {
        Map<String, Object> p = new HashMap<>();
        p.put("P_IDBOLETA", idboleta);
        return p;
    }
}
