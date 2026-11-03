package uni.service;

import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EstadoEnvio;
import uni.entity.TipoServicio;

/**
 * Clasificado: sale a ruta, viaja al hub de destino (interprovincial) o,
 * en recojo en tienda, se entrega en ventanilla. En recojo en tienda
 * también puede observarse si el destinatario no se presenta.
 */
public class EstadoClasificado extends EstadoEnvioBase {

    @Override
    public EstadoEnvio getCodigo() { return EstadoEnvio.CLASIFICADO; }

    @Override
    public void trasladar(EnvioContexto c) { c.avanzar(EstadoEnvio.EN_TRANSITO); }

    @Override
    public void despachar(EnvioContexto c) { c.avanzar(EstadoEnvio.EN_RUTA); }

    @Override
    public void entregar(EnvioContexto c) {
        c.validarFirma();
        c.avanzar(EstadoEnvio.ENTREGADO);
    }

    @Override
    public void observar(EnvioContexto c) {
        if (c.getEnvio().getServicio() != TipoServicio.RECOJO_TIENDA) {
            prohibir(AccionEnvio.OBSERVAR);
        }
        c.registrarIntentoFallido();
    }

    @Override
    public List<AccionEnvio> accionesPermitidas(EnvioContexto c) {
        List<AccionEnvio> l = super.accionesPermitidas(c);
        agregarSiFlujoPermite(c, l, AccionEnvio.TRASLADAR, AccionEnvio.DESPACHAR, AccionEnvio.ENTREGAR);
        if (c.getEnvio().getServicio() == TipoServicio.RECOJO_TIENDA) {
            l.add(AccionEnvio.OBSERVAR);
        }
        return l;
    }
}
