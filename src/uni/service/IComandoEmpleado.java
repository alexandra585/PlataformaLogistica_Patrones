package uni.service;

/**
 * PATRÓN COMMAND (Sesión 13) - Command.
 * Igual que la interfaz Comando del ejemplo de clase: un único método
 * ejecutar(). Cada acción sobre el empleado es un objeto independiente.
 */
public interface IComandoEmpleado {

    /** Ejecuta la acción y devuelve el mensaje que se muestra al usuario. */
    String ejecutar() throws Exception;

    /** Nombre corto para la bitácora del Invoker. */
    String getDescripcion();
}
