package uni.entity;

import java.time.LocalDateTime;

/** Registro inmutable del historial de estados (trazabilidad). */
public class SeguimientoTo {

    private final String codigoEnvio;
    private final EstadoEnvio estado;
    private final String ubicacion;
    private final String observacion;
    private final String idempleado;
    private final LocalDateTime fechahora;

    public SeguimientoTo(String codigoEnvio, EstadoEnvio estado, String ubicacion,
                         String observacion, String idempleado, LocalDateTime fechahora) {
        this.codigoEnvio = codigoEnvio;
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.observacion = observacion;
        this.idempleado = idempleado;
        this.fechahora = fechahora;
    }

    public String getCodigoEnvio() { return codigoEnvio; }
    public EstadoEnvio getEstado() { return estado; }
    public String getUbicacion() { return ubicacion; }
    public String getObservacion() { return observacion; }
    public String getIdempleado() { return idempleado; }
    public LocalDateTime getFechahora() { return fechahora; }

    @Override
    public String toString() {
        return String.format("%-12s | %-22s | %s", estado, ubicacion, observacion);
    }
}
