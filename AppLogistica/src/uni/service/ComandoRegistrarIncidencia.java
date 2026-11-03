package uni.service;

/**
 * COMMAND concreto: el repartidor registra una incidencia de entrega.
 * Al deshacer se restaura el estado y el número de intentos; el registro
 * de la incidencia y del historial se conserva (trazabilidad).
 */
public class ComandoRegistrarIncidencia extends ComandoEnvioBase {

    private final String motivo;
    private final String descripcion;
    private final String ubicacion;

    public ComandoRegistrarIncidencia(ISeguimientoService receptor, String codigoEnvio,
                                      String motivo, String descripcion, String ubicacion) {
        super(receptor, codigoEnvio);
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
    }

    @Override
    protected void accion() throws Exception {
        receptor.registrarIncidencia(codigoEnvio, motivo, descripcion, ubicacion);
    }

    @Override
    public String getNombre() {
        return "INCIDENCIA";
    }

    @Override
    public String getDescripcion() {
        return "Incidencia " + motivo + " en " + codigoEnvio;
    }
}
