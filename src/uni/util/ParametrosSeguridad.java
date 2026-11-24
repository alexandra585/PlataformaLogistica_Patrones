package uni.util;

/**
 * Reglas de seguridad del login en un solo lugar (evita el antipatrón
 * "Magic Numbers" visto en la Sesión 14).
 */
public final class ParametrosSeguridad {

    /** Intentos fallidos seguidos permitidos antes de bloquear la cuenta. */
    public static final int MAX_INTENTOS = 3;

    /** Minutos que la cuenta permanece bloqueada (el administrador puede desbloquearla antes). */
    public static final int MINUTOS_BLOQUEO = 5;

    private ParametrosSeguridad() {
    }
}
