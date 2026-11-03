package uni.entity;

import java.time.LocalDateTime;

/**
 * PATRÓN OBSERVER - información que el sujeto (EnvioContexto) entrega a
 * cada observador cuando un envío cambia de estado. Es inmutable.
 */
public class EventoEnvio {

    private final EnvioTo envio;
    private final EstadoEnvio estadoAnterior;
    private final EstadoEnvio estadoNuevo;
    private final String ubicacion;
    private final String observacion;
    private final String idempleado;
    private final boolean reversion;          // true cuando lo origina un "deshacer"
    private final LocalDateTime fechahora = LocalDateTime.now();

    public EventoEnvio(EnvioTo envio, EstadoEnvio estadoAnterior, EstadoEnvio estadoNuevo,
                       String ubicacion, String observacion, String idempleado, boolean reversion) {
        this.envio = envio;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.ubicacion = ubicacion;
        this.observacion = observacion;
        this.idempleado = idempleado;
        this.reversion = reversion;
    }

    public EnvioTo getEnvio() { return envio; }
    public String getCodigo() { return envio.getCodigo(); }
    public EstadoEnvio getEstadoAnterior() { return estadoAnterior; }
    public EstadoEnvio getEstadoNuevo() { return estadoNuevo; }
    public String getUbicacion() { return ubicacion; }
    public String getObservacion() { return observacion; }
    public String getIdempleado() { return idempleado; }
    public boolean isReversion() { return reversion; }
    public LocalDateTime getFechahora() { return fechahora; }

    @Override
    public String toString() {
        return String.format("%s: %s -> %s%s", getCodigo(), estadoAnterior, estadoNuevo,
                reversion ? " (reversión)" : "");
    }
}
