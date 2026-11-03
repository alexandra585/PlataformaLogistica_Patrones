package uni.service;

import uni.entity.AccionEnvio;

/** COMMAND concreto: recoger, trasladar, clasificar, despachar o devolver. */
public class ComandoCambiarEstado extends ComandoEnvioBase {

    private final AccionEnvio accion;
    private final String ubicacion;
    private final String observacion;

    public ComandoCambiarEstado(ISeguimientoService receptor, String codigoEnvio,
                                AccionEnvio accion, String ubicacion, String observacion) {
        super(receptor, codigoEnvio);
        this.accion = accion;
        this.ubicacion = ubicacion;
        this.observacion = observacion;
    }

    @Override
    protected void accion() throws Exception {
        receptor.actualizarEstado(codigoEnvio, accion.getEstadoDestino(), ubicacion, observacion);
    }

    @Override
    public String getNombre() {
        return accion.name();
    }

    @Override
    public String getDescripcion() {
        return accion.getEtiqueta() + " " + codigoEnvio + (ubicacion == null ? "" : " en " + ubicacion);
    }
}
