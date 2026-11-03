package uni.reportes;

import java.util.List;

/**
 * RefinedAbstraction: productividad de reparto por repartidor
 * (entregas y envíos observados). Filas {repartidor, entregas, observados}.
 */
public class ReporteProductividadReparto extends Reporte {

    private final List<Object[]> datos;

    public ReporteProductividadReparto(IFormatoReporte formato, List<Object[]> datos) {
        super(formato);
        this.datos = datos;
    }

    @Override
    protected String getTitulo() {
        return "Productividad de reparto";
    }

    @Override
    protected void escribirContenido() {
        formato.encabezado("Repartidor", "Entregas", "Observados", "Efectividad");
        for (Object[] d : datos) {
            int entregas = (Integer) d[1];
            int observados = (Integer) d[2];
            int total = entregas + observados;
            formato.fila(d[0], entregas, observados,
                    total == 0 ? "-" : String.format("%.1f %%", 100.0 * entregas / total));
        }
    }
}
