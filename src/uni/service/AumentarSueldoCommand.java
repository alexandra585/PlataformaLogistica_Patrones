package uni.service;

import uni.util.HistorialEmpleado;

/**
 * COMMAND (Sesión 13) - EJERCICIO RESUELTO de la clase:
 * "agregar un nuevo comando AumentarSueldoCommand que incremente el sueldo
 * del empleado en un porcentaje".
 * Antes de modificar el estado pide un memento al Originator y lo entrega al
 * Caretaker, de modo que el aumento siempre puede revertirse (Command + Memento).
 */
public class AumentarSueldoCommand implements IComandoEmpleado {

    private final EmpleadoNomina empleado;
    private final double porcentaje;
    private final HistorialEmpleado historial;

    public AumentarSueldoCommand(EmpleadoNomina empleado, double porcentaje, HistorialEmpleado historial) {
        this.empleado = empleado;
        this.porcentaje = porcentaje;
        this.historial = historial;
    }

    @Override
    public String ejecutar() throws Exception {
        double antes = empleado.getSueldo();
        EmpleadoMemento previo = empleado.guardar("Antes de aumento " + porcentaje + " %");
        empleado.aumentarSueldo(porcentaje);
        historial.guardar(previo);
        return String.format("Sueldo aumentado %.1f %%: S/ %.2f -> S/ %.2f", porcentaje, antes, empleado.getSueldo());
    }

    @Override
    public String getDescripcion() {
        return "Aumentar sueldo " + porcentaje + " %";
    }
}
