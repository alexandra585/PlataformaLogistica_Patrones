package uni.service;

import java.util.ArrayList;
import java.util.List;
import uni.entity.EmpleadoTo;

/** Servicio que arma el contexto laboral del empleado y le suscribe los observadores. */
public class GestionLaboralService {

    private final ILectura<EmpleadoTo> empleadoDao;
    private final List<IObservadorEmpleado> observadores = new ArrayList<>();

    public GestionLaboralService(ILectura<EmpleadoTo> empleadoDao) {
        this.empleadoDao = empleadoDao;
    }

    public void agregarObservador(IObservadorEmpleado o) {
        observadores.add(o);
    }

    public EmpleadoContexto cargar(String idempleado) throws Exception {
        EmpleadoTo e = empleadoDao.find(idempleado);
        if (e == null) {
            throw new IllegalArgumentException("Empleado no existe: " + idempleado);
        }
        EmpleadoContexto c = new EmpleadoContexto(e);
        for (IObservadorEmpleado o : observadores) {
            c.suscribir(o);
        }
        return c;
    }

    /** @return texto de mostrarEstado() después del cambio. */
    public String cambiarEstado(String idempleado, String accion, String motivo) throws Exception {
        EmpleadoContexto c = cargar(idempleado);
        c.ejecutar(accion, motivo);
        return c.mostrarEstado();
    }
}
