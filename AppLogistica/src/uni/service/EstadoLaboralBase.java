package uni.service;

import java.util.ArrayList;
import java.util.List;
import uni.util.TransicionInvalidaException;

/** Comportamiento por defecto: ninguna transición está permitida. */
public abstract class EstadoLaboralBase implements IEstadoLaboral {

    protected void prohibir(String accion) {
        throw new TransicionInvalidaException("No se puede " + accion + ": el empleado está " + getCodigo() + ".");
    }

    @Override
    public void enviarVacaciones(EmpleadoContexto c) { prohibir("enviar de vacaciones"); }

    @Override
    public void reincorporar(EmpleadoContexto c) { prohibir("reincorporar"); }

    @Override
    public void cesar(EmpleadoContexto c) { prohibir("cesar"); }

    @Override
    public List<String> accionesPermitidas() { return new ArrayList<>(); }

    @Override
    public String toString() { return getCodigo(); }

    /** Convierte el valor de la columna empleados.estado_laboral en el objeto estado. */
    public static IEstadoLaboral de(String codigo) {
        if ("VACACIONES".equals(codigo)) {
            return new EstadoLaboralVacaciones();
        }
        if ("INACTIVO".equals(codigo)) {
            return new EstadoLaboralInactivo();
        }
        return new EstadoLaboralActivo();
    }
}
