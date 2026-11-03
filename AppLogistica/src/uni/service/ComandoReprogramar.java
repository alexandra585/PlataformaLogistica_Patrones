package uni.service;

import java.time.LocalDate;

/** COMMAND concreto: reprogramar la entrega para una nueva fecha. */
public class ComandoReprogramar extends ComandoEnvioBase {

    private final LocalDate nuevaFecha;
    private final String observacion;

    public ComandoReprogramar(ISeguimientoService receptor, String codigoEnvio,
                              LocalDate nuevaFecha, String observacion) {
        super(receptor, codigoEnvio);
        this.nuevaFecha = nuevaFecha;
        this.observacion = observacion;
    }

    @Override
    protected void accion() throws Exception {
        receptor.reprogramar(codigoEnvio, nuevaFecha, observacion);
    }

    @Override
    public String getNombre() {
        return "REPROGRAMAR";
    }

    @Override
    public String getDescripcion() {
        return "Reprogramar " + codigoEnvio + " para " + nuevaFecha;
    }
}
