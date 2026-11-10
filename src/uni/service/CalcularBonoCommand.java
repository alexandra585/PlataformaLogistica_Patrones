package uni.service;

/** COMMAND (Sesión 13) - comando concreto: bono del 10 % del sueldo. */
public class CalcularBonoCommand implements IComandoEmpleado {

    private final EmpleadoNomina empleado;

    public CalcularBonoCommand(EmpleadoNomina empleado) {
        this.empleado = empleado;
    }

    @Override
    public String ejecutar() {
        return String.format("Bono calculado (10 %%): S/ %.2f", empleado.calcularBono());
    }

    @Override
    public String getDescripcion() {
        return "Calcular bono";
    }
}
