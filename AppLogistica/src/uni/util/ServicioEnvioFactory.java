package uni.util;

import uni.entity.TipoServicio;
import uni.service.IServicioEnvio;
import uni.service.ServicioEstandar;
import uni.service.ServicioExpress;
import uni.service.ServicioInterprovincial;
import uni.service.ServicioProgramado;
import uni.service.ServicioRecojoTienda;

/**
 * PATRÓN FACTORY (Factory Method parametrizado).
 * Único lugar del sistema que conoce las clases concretas de servicio.
 * Controladores y fachadas piden un IServicioEnvio sin usar "new".
 */
public class ServicioEnvioFactory {

    public IServicioEnvio crearServicio(TipoServicio tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de servicio no indicado.");
        }
        switch (tipo) {
            case EXPRESS:
                return new ServicioExpress();
            case ESTANDAR:
                return new ServicioEstandar();
            case PROGRAMADO:
                return new ServicioProgramado();
            case RECOJO_TIENDA:
                return new ServicioRecojoTienda();
            case INTERPROVINCIAL:
                return new ServicioInterprovincial();
            default:
                throw new IllegalArgumentException("Servicio no soportado: " + tipo);
        }
    }
}
