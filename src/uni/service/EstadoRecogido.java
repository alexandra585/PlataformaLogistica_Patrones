package uni.service;

import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EstadoEnvio;

/**
 * Recogido: según el servicio pasa a tránsito (estándar, recojo en tienda),
 * a clasificación (programado, interprovincial) o directo a ruta (express).
 * La decisión la toma el flujo definido por cada IServicioEnvio (Factory).
 */
public class EstadoRecogido extends EstadoEnvioBase {

    @Override
    public EstadoEnvio getCodigo() { return EstadoEnvio.RECOGIDO; }

    @Override
    public void trasladar(EnvioContexto c) { c.avanzar(EstadoEnvio.EN_TRANSITO); }

    @Override
    public void clasificar(EnvioContexto c) { c.avanzar(EstadoEnvio.CLASIFICADO); }

    @Override
    public void despachar(EnvioContexto c) { c.avanzar(EstadoEnvio.EN_RUTA); }

    @Override
    public List<AccionEnvio> accionesPermitidas(EnvioContexto c) {
        List<AccionEnvio> l = super.accionesPermitidas(c);
        agregarSiFlujoPermite(c, l, AccionEnvio.TRASLADAR, AccionEnvio.CLASIFICAR, AccionEnvio.DESPACHAR);
        return l;
    }
}
