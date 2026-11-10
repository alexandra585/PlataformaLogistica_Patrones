package uni.service;

/** COMMAND (Sesión 13) - comando concreto: muestra los datos del empleado. */
public class MostrarDatosCommand implements IComandoEmpleado {

    private final EmpleadoNomina empleado;

    public MostrarDatosCommand(EmpleadoNomina empleado) {
        this.empleado = empleado;
    }

    @Override
    public String ejecutar() {
        return empleado.mostrarDatos();
    }

    @Override
    public String getDescripcion() {
        return "Mostrar datos";
    }
}
