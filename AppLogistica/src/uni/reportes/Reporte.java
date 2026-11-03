package uni.reportes;

/**
 * PATRÓN BRIDGE - Abstraction.
 * Mantiene una referencia (el "puente") al formato de salida.
 */
public abstract class Reporte {

    protected IFormatoReporte formato;

    protected Reporte(IFormatoReporte formato) {
        this.formato = formato;
    }

    /** El formato puede cambiarse en tiempo de ejecución. */
    public void setFormato(IFormatoReporte formato) {
        this.formato = formato;
    }

    public final String generar() {
        formato.iniciar(getTitulo());
        escribirContenido();
        return formato.finalizar();
    }

    protected abstract String getTitulo();

    protected abstract void escribirContenido();
}
