package uni.util;

import uni.entity.EmpleadoTo;

/**
 * PATRÓN SINGLETON (instanciación temprana).
 * Guarda el usuario autenticado en LoginView para que cualquier capa
 * (Proxy de seguridad, auditoría, formularios) conozca quién opera.
 */
public final class SesionUsuario {

    private static final SesionUsuario INSTANCIA = new SesionUsuario();

    private EmpleadoTo usuario;

    private SesionUsuario() {
    }

    public static SesionUsuario getInstancia() {
        return INSTANCIA;
    }

    public void iniciar(EmpleadoTo usuario) {
        this.usuario = usuario;
    }

    public void cerrar() {
        this.usuario = null;
    }

    public EmpleadoTo getUsuario() {
        if (usuario == null) {
            throw new IllegalStateException("No hay sesión iniciada.");
        }
        return usuario;
    }
}
