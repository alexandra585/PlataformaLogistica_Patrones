package uni.service;

import java.util.Arrays;
import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EstadoEnvio;

/** En ruta con el repartidor: se entrega o se registra una incidencia. */
public class EstadoEnRuta extends EstadoEnvioBase {

    @Override
    public EstadoEnvio getCodigo() { return EstadoEnvio.EN_RUTA; }

    @Override
    public void entregar(EnvioContexto c) {
        c.validarFirma();                       // regla: si requiere firma, debe existir
        c.avanzar(EstadoEnvio.ENTREGADO);
    }

    @Override
    public void observar(EnvioContexto c) {
        c.registrarIntentoFallido();            // intentos++ y pasa a OBSERVADO
    }

    @Override
    public List<AccionEnvio> accionesPermitidas(EnvioContexto c) {
        return Arrays.asList(AccionEnvio.ENTREGAR, AccionEnvio.OBSERVAR);
    }
}
