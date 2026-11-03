package uni.util;

import uni.entity.AccionEnvio;
import uni.entity.EstadoEnvio;

/** Se lanza cuando el estado actual de un envío no admite la acción solicitada (State). */
public class TransicionInvalidaException extends IllegalStateException {

    public TransicionInvalidaException(EstadoEnvio estado, AccionEnvio accion) {
        super("Acción \"" + accion.getEtiqueta() + "\" no permitida: el envío está " + estado + ".");
    }

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
