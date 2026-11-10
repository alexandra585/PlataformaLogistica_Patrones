package uni.reportes;

/** RefinedAbstraction: Personal activo con rol, sucursal, área, estado laboral y sueldo. */
public class ReporteListaEmpleados extends Reporte {

    public ReporteListaEmpleados(ISalidaReporte salida) {
        super(salida);
    }

    @Override
    public String getTitulo() {
        return "Lista de empleados";
    }

    @Override
    protected String getPlantilla() {
        return "listaEmpleados.jrxml";
    }
}
