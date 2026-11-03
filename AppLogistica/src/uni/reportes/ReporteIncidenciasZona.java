package uni.reportes;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import uni.entity.IncidenciaTo;

/** RefinedAbstraction: zonas con más incidencias (decisión estratégica). */
public class ReporteIncidenciasZona extends Reporte {

    private final List<IncidenciaTo> incidencias;

    public ReporteIncidenciasZona(IFormatoReporte formato, List<IncidenciaTo> incidencias) {
        super(formato);
        this.incidencias = incidencias;
    }

    @Override
    protected String getTitulo() {
        return "Incidencias por zona";
    }

    @Override
    protected void escribirContenido() {
        Map<String, Integer> conteo = new TreeMap<>();
        for (IncidenciaTo i : incidencias) {
            conteo.merge(i.getZona(), 1, Integer::sum);
        }
        formato.encabezado("Zona", "Incidencias", "% del total");
        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
            double pct = 100.0 * e.getValue() / incidencias.size();
            formato.fila(e.getKey(), e.getValue(), String.format("%.1f %%", pct));
        }
    }
}
