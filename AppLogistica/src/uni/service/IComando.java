package uni.service;

/**
 * PATRÓN COMMAND - Command.
 * Encapsula una acción del personal operativo (recoger, despachar,
 * registrar incidencia, entregar...) como un objeto que se puede
 * ejecutar, deshacer, encolar (modo sin conexión) y registrar en bitácora.
 */
public interface IComando {

    void ejecutar() throws Exception;

    void deshacer() throws Exception;

    String getNombre();

    String getDescripcion();

    String getCodigoEnvio();

    /** Estado antes y después de ejecutar (para la bitácora). */
    String getEstadoPrevio();

    String getEstadoNuevo();
}
