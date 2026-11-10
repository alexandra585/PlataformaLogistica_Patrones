package uni.service;

import java.util.ArrayList;
import java.util.List;
import uni.entity.EmpleadoTo;

/**
 * STATE (Context) + OBSERVER (Subject) para el empleado.
 * Equivale a la clase Empleado del ejercicio de la Sesión 12: delega su
 * comportamiento en el estado laboral y avisa a las áreas interesadas
 * (Recursos Humanos, Operaciones) cuando ese estado cambia.
 */
public class EmpleadoContexto {

    private final EmpleadoTo empleado;
    private IEstadoLaboral estado;
    private final List<IObservadorEmpleado> observadores = new ArrayList<>();
    private String motivo;

    public EmpleadoContexto(EmpleadoTo empleado) {
        this.empleado = empleado;
        this.estado = EstadoLaboralBase.de(empleado.getEstadoLaboral());
    }

    // ------------------------------------------------------------- State
    public void ejecutar(String accion, String motivo) {
        this.motivo = motivo;
        switch (accion) {
            case "VACACIONES":   estado.enviarVacaciones(this); break;
            case "REINCORPORAR": estado.reincorporar(this); break;
            case "CESAR":        estado.cesar(this); break;
            default: throw new IllegalArgumentException("Acción laboral desconocida: " + accion);
        }
    }

    void cambiarEstado(IEstadoLaboral nuevo) {
        String anterior = estado.getCodigo();
        estado = nuevo;
        empleado.setEstadoLaboral(nuevo.getCodigo());
        empleado.setActivo(!"INACTIVO".equals(nuevo.getCodigo()));
        for (IObservadorEmpleado o : new ArrayList<>(observadores)) {
            o.actualizar(empleado, anterior, nuevo.getCodigo(), motivo);
        }
    }

    public String mostrarEstado() {
        return estado.mostrarEstado(empleado);
    }

    public IEstadoLaboral getEstado() {
        return estado;
    }

    public EmpleadoTo getEmpleado() {
        return empleado;
    }

    // ---------------------------------------------------------- Observer
    public void suscribir(IObservadorEmpleado o) {
        observadores.add(o);
    }

    public void desuscribir(IObservadorEmpleado o) {
        observadores.remove(o);
    }
}
