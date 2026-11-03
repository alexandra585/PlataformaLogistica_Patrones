package uni.reportes;

import java.util.List;

/**
 * RefinedAbstraction: % de envíos entregados dentro del plazo prometido.
 * Cada fila de datos: {servicio, totalEntregados, entregadosATiempo}.
 */
public class ReporteCumplimientoTiempos extends Reporte {

    private final List<Object[]> datos;

    public ReporteCumplimientoTiempos(IFormatoReporte formato, List<Object[]> datos) {
        super(formato);
        this.datos = datos;
    }

    @Override
    protected String getTitulo() {
        return "Cumplimiento de tiempos";
    }

    @Override
    protected void escribirContenido() {
        formato.encabezado("Servicio", "Entregados", "A tiempo", "Cumplimiento");
        for (Object[] d : datos) {
            int total = (Integer) d[1];
            int aTiempo = (Integer) d[2];
            formato.fila(d[0], total, aTiempo, total == 0 ? "-" : String.format("%.1f %%", 100.0 * aTiempo / total));
        }
    }
}
