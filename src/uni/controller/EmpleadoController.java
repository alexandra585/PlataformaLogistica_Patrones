package uni.controller;

import java.util.List;
import uni.dao.BloqueoLoginDao;
import uni.dao.EmpleadoDao;
import uni.entity.BloqueoLoginTo;
import uni.service.ControlAccesoLogin;
import uni.service.CuentaBloqueadaException;
import uni.entity.EmpleadoTo;
import uni.util.SesionUsuario;

/** Controlador de empleados y del acceso al sistema (molde AppEmpresa). */
public class EmpleadoController {

    private final EmpleadoDao dao = new EmpleadoDao();
    private final ControlAccesoLogin controlAcceso = new ControlAccesoLogin(new BloqueoLoginDao());

    /**
     * Valida el usuario y, si es correcto, inicia la sesión (Singleton).
     * GRASP CONTROLADOR: la vista (LoginView) solo delega aquí; la regla de
     * los 3 intentos la decide ControlAccesoLogin (Experto en información).
     *
     * @throws CuentaBloqueadaException si la cuenta está bloqueada o se bloquea en este intento
     * @throws SecurityException si la clave es incorrecta (indica los intentos que quedan)
     */
    public boolean EmpleadoValidar(String usuario, String clave) throws Exception {
        controlAcceso.verificarNoBloqueado(usuario);
        EmpleadoTo e = dao.validar(usuario, clave);
        if (e == null) {
            int restantes = controlAcceso.registrarFallo(usuario);   // al 3.er fallo lanza CuentaBloqueadaException
            throw new SecurityException("Usuario o contraseña incorrectos.\nLe quedan " + restantes
                    + " intento(s) antes de que la cuenta se bloquee por "
                    + uni.util.ParametrosSeguridad.MINUTOS_BLOQUEO + " minutos.");
        }
        controlAcceso.registrarExito(usuario);
        // PATRÓN STATE: el estado laboral decide si puede ingresar
        uni.service.IEstadoLaboral estado = uni.service.EstadoLaboralBase.de(e.getEstadoLaboral());
        if (!estado.puedeIniciarSesion()) {
            throw new SecurityException(estado.mostrarEstado(e));
        }
        SesionUsuario.getInstancia().iniciar(e);
        return true;
    }

    public void empleadoGrabar(EmpleadoTo e, boolean nuevo) throws Exception {
        if (e.getNombre() == null || e.getNombre().trim().isEmpty()
                || e.getUsuario() == null || e.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre y usuario son obligatorios.");
        }
        if (nuevo) {
            if (e.getClave() == null || e.getClave().length() < 4) {
                throw new IllegalArgumentException("La clave debe tener al menos 4 caracteres.");
            }
            dao.create(e);
        } else {
            dao.update(e);
        }
    }

    public void empleadoEliminar(String id) throws Exception {
        EmpleadoTo e = new EmpleadoTo();
        e.setIdempleado(id);
        dao.delete(e);
    }

    /** Cambia el estado laboral (State) y notifica a RR. HH. y Operaciones (Observer). */
    public String empleadoCambiarEstado(String id, String accion, String motivo) throws Exception {
        return uni.util.ConfiguracionSistema.getInstancia().getGestionLaboral().cambiarEstado(id, accion, motivo);
    }

    public EmpleadoTo empleadoBuscar(String id) throws Exception {
        return dao.find(id);
    }

    public List<Object[]> historialLaboral(String id) throws Exception {
        return new uni.dao.HistorialLaboralDao().listar(id);
    }

    public List<EmpleadoTo> empleadoListar() throws Exception {
        return dao.readAll();
    }

    // ------------------------------------------------ Sesión 13: Command + Memento

    /** Crea el Receiver/Originator del empleado con su persistencia (DIP). */
    public uni.service.EmpleadoNomina nominaCrear(String id) throws Exception {
        EmpleadoTo e = dao.find(id);
        if (e == null) {
            throw new IllegalArgumentException("No existe el empleado " + id);
        }
        String usuario = uni.util.SesionUsuario.getInstancia().getUsuario() == null ? null
                : uni.util.SesionUsuario.getInstancia().getUsuario().getIdempleado();
        return new uni.service.EmpleadoNomina(e, new uni.dao.EmpleadoDao(), usuario);
    }

    public List<Object[]> historialRemuneracion(String id) throws Exception {
        return new uni.dao.EmpleadoDao().historialRemuneracion(id);
    }

    // ------------------------------------------------ Sesión 17: boleta de pago (GRASP)

    /** Fabricación pura con su persistencia inyectada (DIP). */
    public uni.service.GeneradorBoleta boletaGenerador() {
        return new uni.service.GeneradorBoleta(new uni.dao.BoletaDao());
    }

    public List<Object[]> boletasListar(String id) throws Exception {
        return new uni.dao.BoletaDao().listar(id);
    }

    /** Usuarios con intentos fallidos o bloqueados (Utilitarios > Desbloquear usuarios). */
    public List<BloqueoLoginTo> bloqueosListar() throws Exception {
        return controlAcceso.listarConFallos();
    }

    public long bloqueoSegundosRestantes(BloqueoLoginTo b) {
        return controlAcceso.segundosRestantes(b);
    }

    public void usuarioDesbloquear(String usuario) throws Exception {
        controlAcceso.desbloquear(usuario);
    }
}
