package uni.service;

/** COMMAND (Sesión 13) - comando concreto: sueldo final = sueldo + bono. */
public class CalcularSueldoCommand implements IComandoEmpleado {

    private final EmpleadoNomina empleado;

    public CalcularSueldoCommand(EmpleadoNomina empleado) {
        this.empleado = empleado;
    }

    @Override
    public String ejecutar() {
        return String.format("Sueldo final: S/ %.2f", empleado.calcularSueldoFinal());
    }

    @Override
    public String getDescripcion() {
        return "Calcular sueldo final";
    }
}
