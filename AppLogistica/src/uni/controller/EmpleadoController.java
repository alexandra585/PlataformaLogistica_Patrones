package uni.controller;

import java.util.List;
import uni.dao.EmpleadoDao;
import uni.entity.EmpleadoTo;
import uni.util.SesionUsuario;

/** Controlador de empleados y del acceso al sistema (molde AppEmpresa). */
public class EmpleadoController {

    private final EmpleadoDao dao = new EmpleadoDao();

    /** Valida el usuario y, si es correcto, inicia la sesión (Singleton). */
    public boolean EmpleadoValidar(String usuario, String clave) throws Exception {
        EmpleadoTo e = dao.validar(usuario, clave);
        if (e == null) {
            return false;
        }
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
}
