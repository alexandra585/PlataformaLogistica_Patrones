package uni.service;

import java.util.EnumMap;
import java.util.Map;
import uni.entity.EstadoEnvio;

/**
 * Obtiene el objeto estado (State) que corresponde al valor guardado en la
 * columna envios.estado. Los estados no guardan datos propios (todo está
 * en el contexto), por eso se comparte una sola instancia de cada uno.
 */
public final class CatalogoEstados {

    private static final Map<EstadoEnvio, IEstadoEnvio> ESTADOS = new EnumMap<>(EstadoEnvio.class);

    static {
        registrar(new EstadoRegistrado());
        registrar(new EstadoRecogido());
        registrar(new EstadoEnTransito());
        registrar(new EstadoClasificado());
        registrar(new EstadoEnRuta());
        registrar(new EstadoEntregado());
        registrar(new EstadoObservado());
        registrar(new EstadoReprogramado());
        registrar(new EstadoDevuelto());
    }

    private CatalogoEstados() {
    }

    private static void registrar(IEstadoEnvio e) {
        ESTADOS.put(e.getCodigo(), e);
    }

    public static IEstadoEnvio de(EstadoEnvio codigo) {
        return ESTADOS.get(codigo);
    }
}
