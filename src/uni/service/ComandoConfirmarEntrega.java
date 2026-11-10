package uni.service;

/**
 * COMMAND concreto: confirmar la entrega (con firma digital si se requiere).
 * Su deshacer pasa por el Proxy, que solo lo permite a supervisores.
 */
public class ComandoConfirmarEntrega extends ComandoEnvioBase {

    private final String dniReceptor;
    private final String ubicacion;

    public ComandoConfirmarEntrega(ISeguimientoService receptor, String codigoEnvio,
                                   String dniReceptor, String ubicacion) {
        super(receptor, codigoEnvio);
        this.dniReceptor = dniReceptor;
        this.ubicacion = ubicacion;
    }

    @Override
    protected void accion() throws Exception {
        receptor.confirmarEntrega(codigoEnvio, dniReceptor, ubicacion);
    }

    @Override
    public String getNombre() {
        return "ENTREGAR";
    }

    @Override
    public String getDescripcion() {
        return "Entregar " + codigoEnvio + (ubicacion == null ? "" : " en " + ubicacion);
    }
}
