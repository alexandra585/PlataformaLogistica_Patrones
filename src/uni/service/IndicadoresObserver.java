package uni.service;

import java.util.EnumMap;
import java.util.Map;
import uni.entity.EstadoEnvio;
import uni.entity.EventoEnvio;

/**
 * OBSERVER concreto: acumula indicadores operativos en tiempo real
 * (productividad de reparto, incidencias) para la barra de estado y para
 * la toma de decisiones, sin consultar la base de datos.
 */
public class IndicadoresObserver implements IObservadorEnvio {

    private final Map<EstadoEnvio, Integer> conteo = new EnumMap<>(EstadoEnvio.class);
    private int reversiones;

    @Override
    public synchronized void actualizar(EventoEnvio e) {
        if (e.isReversion()) {
            reversiones++;
            conteo.merge(e.getEstadoAnterior(), -1, Integer::sum);
            return;
        }
        conteo.merge(e.getEstadoNuevo(), 1, Integer::sum);
    }

    public synchronized int get(EstadoEnvio estado) {
        return Math.max(0, conteo.getOrDefault(estado, 0));
    }

    public synchronized String getResumen() {
        return String.format("Sesión: %d entregados | %d en ruta | %d observados | %d devueltos | %d reversiones",
                get(EstadoEnvio.ENTREGADO), get(EstadoEnvio.EN_RUTA), get(EstadoEnvio.OBSERVADO),
                get(EstadoEnvio.DEVUELTO), reversiones);
    }

    @Override
    public String getNombre() {
        return "Indicadores en tiempo real";
    }
}
