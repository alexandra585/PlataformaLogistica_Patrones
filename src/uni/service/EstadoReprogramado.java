package uni.service;

import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EstadoEnvio;

/**
 * Reprogramado: vuelve a salir a ruta en la nueva fecha. En recojo en
 * tienda no hay ruta: el paquete vuelve a estar disponible (CLASIFICADO).
 */
public class EstadoReprogramado extends EstadoEnvioBase {

    @Override
    public EstadoEnvio getCodigo() { return EstadoEnvio.REPROGRAMADO; }

    @Override
    public void despachar(EnvioContexto c) {
        if (!c.flujoContiene(EstadoEnvio.EN_RUTA)) {
            prohibir(AccionEnvio.DESPACHAR);
        }
        c.retomarEn(EstadoEnvio.EN_RUTA);
    }

    @Override
    public void clasificar(EnvioContexto c) {
        if (c.flujoContiene(EstadoEnvio.EN_RUTA)) {
            prohibir(AccionEnvio.CLASIFICAR);
        }
        c.retomarEn(EstadoEnvio.CLASIFICADO);
    }

    @Override
    public void devolver(EnvioContexto c) {
        c.cambiarEstado(EstadoEnvio.DEVUELTO);
    }

    @Override
    public List<AccionEnvio> accionesPermitidas(EnvioContexto c) {
        List<AccionEnvio> l = super.accionesPermitidas(c);
        l.add(c.flujoContiene(EstadoEnvio.EN_RUTA) ? AccionEnvio.DESPACHAR : AccionEnvio.CLASIFICAR);
        l.add(AccionEnvio.DEVOLVER);
        return l;
    }
}
