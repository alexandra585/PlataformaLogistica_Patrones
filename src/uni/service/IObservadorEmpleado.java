package uni.service;

import uni.entity.EmpleadoTo;

/** PATRÓN OBSERVER - observadores de los cambios laborales de un empleado. */
public interface IObservadorEmpleado {

    void actualizar(EmpleadoTo empleado, String estadoAnterior, String estadoNuevo, String motivo);

    String getNombre();
}
