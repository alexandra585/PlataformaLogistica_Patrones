package uni.service;

import java.time.LocalDate;
import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EstadoEnvio;

/**
 * PATRÓN STATE - State.
 * Cada estado del ciclo de vida de un envío decide qué acciones acepta y a
 * qué estado conduce cada una. El contexto (EnvioContexto) delega en el
 * estado actual en lugar de usar un switch gigante sobre el enum.
 */
public interface IEstadoEnvio {

    EstadoEnvio getCodigo();

    void recoger(EnvioContexto c);

    void trasladar(EnvioContexto c);

    void clasificar(EnvioContexto c);

    void despachar(EnvioContexto c);

    void entregar(EnvioContexto c);

    void observar(EnvioContexto c);

    void reprogramar(EnvioContexto c, LocalDate nuevaFecha);

    void devolver(EnvioContexto c);

    /** Acciones válidas en este estado (habilitan los botones de la vista). */
    List<AccionEnvio> accionesPermitidas(EnvioContexto c);

    boolean esFinal();
}
