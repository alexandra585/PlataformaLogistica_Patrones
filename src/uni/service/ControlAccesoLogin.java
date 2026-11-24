package uni.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import uni.entity.BloqueoLoginTo;
import uni.util.ParametrosSeguridad;

/**
 * Seguridad del login: bloquea la cuenta tras {@value ParametrosSeguridad#MAX_INTENTOS}
 * contraseñas incorrectas seguidas durante {@value ParametrosSeguridad#MINUTOS_BLOQUEO} minutos.
 *
 * GRASP EXPERTO EN INFORMACIÓN: es quien conoce las reglas (máximo de intentos,
 * duración del bloqueo) y el contador de cada usuario, así que es quien decide.
 * GRASP BAJO ACOPLAMIENTO / DIP: guarda a través de IRegistroIntentosLogin, por eso
 * se prueba con un registro en memoria sin MySQL. El reloj (Clock) también se
 * inyecta para simular el paso de los 5 minutos en las pruebas.
 */
public class ControlAccesoLogin {

    private final IRegistroIntentosLogin registro;
    private final Clock reloj;

    public ControlAccesoLogin(IRegistroIntentosLogin registro) {
        this(registro, Clock.systemDefaultZone());
    }

    public ControlAccesoLogin(IRegistroIntentosLogin registro, Clock reloj) {
        this.registro = registro;
        this.reloj = reloj;
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }

    /**
     * Antes de comparar la clave: si la cuenta sigue bloqueada lanza
     * CuentaBloqueadaException; si el bloqueo ya venció, reinicia el contador.
     */
    public void verificarNoBloqueado(String usuario) throws Exception {
        BloqueoLoginTo b = registro.buscar(usuario);
        if (b == null) {
            return;
        }
        if (b.estaBloqueado(ahora())) {
            long seg = Math.max(1, Duration.between(ahora(), b.getBloqueadoHasta()).getSeconds());
            registro.registrarIntento(usuario, false, "INTENTO CON CUENTA BLOQUEADA");
            throw new CuentaBloqueadaException(mensajeBloqueo(seg), seg);
        }
        if (b.getBloqueadoHasta() != null) {            // el bloqueo ya venció
            b.setBloqueadoHasta(null);
            b.setIntentosFallidos(0);
            registro.guardar(b);
        }
    }

    /**
     * Registra una contraseña incorrecta. Devuelve los intentos que le quedan;
     * al llegar al máximo bloquea la cuenta y lanza CuentaBloqueadaException.
     */
    public int registrarFallo(String usuario) throws Exception {
        BloqueoLoginTo b = registro.buscar(usuario);
        if (b == null) {
            b = new BloqueoLoginTo(usuario, 0, null, null);
        }
        b.setIntentosFallidos(b.getIntentosFallidos() + 1);
        b.setUltimoFallo(ahora());
        int restantes = ParametrosSeguridad.MAX_INTENTOS - b.getIntentosFallidos();
        if (restantes <= 0) {
            b.setBloqueadoHasta(ahora().plusMinutes(ParametrosSeguridad.MINUTOS_BLOQUEO));
            registro.guardar(b);
            registro.registrarIntento(usuario, false, "CUENTA BLOQUEADA ("
                    + ParametrosSeguridad.MAX_INTENTOS + " INTENTOS FALLIDOS)");
            long seg = ParametrosSeguridad.MINUTOS_BLOQUEO * 60L;
            throw new CuentaBloqueadaException(mensajeBloqueo(seg), seg);
        }
        registro.guardar(b);
        registro.registrarIntento(usuario, false, "CLAVE INCORRECTA");
        return restantes;
    }

    /** Ingreso correcto: el contador vuelve a cero. */
    public void registrarExito(String usuario) throws Exception {
        BloqueoLoginTo b = registro.buscar(usuario);
        if (b != null && (b.getIntentosFallidos() > 0 || b.getBloqueadoHasta() != null)) {
            b.setIntentosFallidos(0);
            b.setBloqueadoHasta(null);
            registro.guardar(b);
        }
        registro.registrarIntento(usuario, true, "INGRESO CORRECTO");
    }

    /** Solo el ADMINISTRADOR (lo controla el menú de PrincipalView). */
    public void desbloquear(String usuario) throws Exception {
        registro.desbloquear(usuario);
    }

    public List<BloqueoLoginTo> listarConFallos() throws Exception {
        return registro.listarConFallos();
    }

    /** Segundos de bloqueo que le quedan al usuario (0 si no está bloqueado). */
    public long segundosRestantes(BloqueoLoginTo b) {
        return b.estaBloqueado(ahora()) ? Duration.between(ahora(), b.getBloqueadoHasta()).getSeconds() : 0;
    }

    private static String mensajeBloqueo(long segundos) {
        long min = (segundos + 59) / 60;
        return "La cuenta está bloqueada por " + ParametrosSeguridad.MAX_INTENTOS
                + " intentos fallidos.\nIntente nuevamente en " + min + " minuto(s) "
                + "o pida al administrador que la desbloquee.";
    }
}
