package uni.service;

/** Se lanza cuando la cuenta está bloqueada por exceso de intentos fallidos. */
public class CuentaBloqueadaException extends SecurityException {

    private static final long serialVersionUID = 1L;
    private final long segundosRestantes;

    public CuentaBloqueadaException(String mensaje, long segundosRestantes) {
        super(mensaje);
        this.segundosRestantes = segundosRestantes;
    }

    public long getSegundosRestantes() {
        return segundosRestantes;
    }
}
