package uni.service;

import java.util.List;
import uni.entity.EmpleadoTo;

/**
 * PATRÓN STATE aplicado al empleado (ejercicio de la Sesión 12, adaptado a
 * LogiRuta): el estado laboral decide si el trabajador puede ingresar al
 * sistema, si puede recibir envíos asignados y a qué estados puede pasar.
 */
public interface IEstadoLaboral {

    String getCodigo();

    boolean puedeIniciarSesion();

    boolean puedeRecibirEnvios();

    /** Comportamiento que cambia según el estado (mostrarEstado() de la clase). */
    String mostrarEstado(EmpleadoTo e);

    void enviarVacaciones(EmpleadoContexto c);

    void reincorporar(EmpleadoContexto c);

    void cesar(EmpleadoContexto c);

    /** Acciones válidas: VACACIONES, REINCORPORAR, CESAR. */
    List<String> accionesPermitidas();
}
