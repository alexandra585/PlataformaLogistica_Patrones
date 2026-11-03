package uni.entity;

/**
 * Acciones que el personal operativo puede solicitar sobre un envío.
 * Cada estado (patrón State) decide cuáles acepta y a qué estado lleva.
 */
public enum AccionEnvio {
    RECOGER("Recoger", EstadoEnvio.RECOGIDO),
    TRASLADAR("Trasladar", EstadoEnvio.EN_TRANSITO),
    CLASIFICAR("Clasificar", EstadoEnvio.CLASIFICADO),
    DESPACHAR("Despachar", EstadoEnvio.EN_RUTA),
    ENTREGAR("Entregar", EstadoEnvio.ENTREGADO),
    OBSERVAR("Incidencia", EstadoEnvio.OBSERVADO),
    REPROGRAMAR("Reprogramar", EstadoEnvio.REPROGRAMADO),
    DEVOLVER("Devolver", EstadoEnvio.DEVUELTO);

    private final String etiqueta;
    private final EstadoEnvio estadoDestino;

    AccionEnvio(String etiqueta, EstadoEnvio estadoDestino) {
        this.etiqueta = etiqueta;
        this.estadoDestino = estadoDestino;
    }

    public String getEtiqueta() { return etiqueta; }

    public EstadoEnvio getEstadoDestino() { return estadoDestino; }

    /** Acción que conduce al estado indicado (compatibilidad con actualizarEstado del Cap. V). */
    public static AccionEnvio hacia(EstadoEnvio destino) {
        for (AccionEnvio a : values()) {
            if (a.estadoDestino == destino) {
                return a;
            }
        }
        throw new IllegalArgumentException("Ninguna acción conduce a " + destino);
    }
}
