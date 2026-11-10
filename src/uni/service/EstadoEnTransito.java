package uni.service;

import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EstadoEnvio;

/** En tránsito hacia el almacén, hub o tienda: lo siguiente es clasificar. */
public class EstadoEnTransito extends EstadoEnvioBase {

    @Override
    public EstadoEnvio getCodigo() { return EstadoEnvio.EN_TRANSITO; }

    @Override
    public void clasificar(EnvioContexto c) { c.avanzar(EstadoEnvio.CLASIFICADO); }

    @Override
    public List<AccionEnvio> accionesPermitidas(EnvioContexto c) {
        List<AccionEnvio> l = super.accionesPermitidas(c);
        agregarSiFlujoPermite(c, l, AccionEnvio.CLASIFICAR);
        return l;
    }
}
