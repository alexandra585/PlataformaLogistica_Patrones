package uni.service;

import uni.util.HistorialEmpleado;

/**
 * COMMAND (Sesión 13) - comando concreto: cambio de área.
 * Igual que AumentarSueldoCommand, guarda un memento previo en el Caretaker.
 */
public class CambiarAreaCommand implements IComandoEmpleado {

    private final EmpleadoNomina empleado;
    private final String nuevaArea;
    private final HistorialEmpleado historial;

    public CambiarAreaCommand(EmpleadoNomina empleado, String nuevaArea, HistorialEmpleado historial) {
        this.empleado = empleado;
        this.nuevaArea = nuevaArea;
        this.historial = historial;
    }

    @Override
    public String ejecutar() throws Exception {
        String antes = empleado.getArea();
        EmpleadoMemento previo = empleado.guardar("Antes de cambio de área (" + antes + ")");
        empleado.cambiarArea(nuevaArea);
        historial.guardar(previo);
        return "Área cambiada: " + antes + " -> " + empleado.getArea();
    }

    @Override
    public String getDescripcion() {
        return "Cambiar área a " + nuevaArea;
    }
}
