package uni.service;

import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EstadoEnvio;

/** Estado inicial: el envío solo puede ser recogido. */
public class EstadoRegistrado extends EstadoEnvioBase {

    @Override
    public EstadoEnvio getCodigo() { return EstadoEnvio.REGISTRADO; }

    @Override
    public void recoger(EnvioContexto c) {
        c.avanzar(EstadoEnvio.RECOGIDO);
    }

    @Override
    public List<AccionEnvio> accionesPermitidas(EnvioContexto c) {
        List<AccionEnvio> l = super.accionesPermitidas(c);
        agregarSiFlujoPermite(c, l, AccionEnvio.RECOGER);
        return l;
    }
}
