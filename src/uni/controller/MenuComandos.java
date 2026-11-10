package uni.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import uni.service.IComandoEmpleado;

/**
 * PATRÓN COMMAND (Sesión 13) - Invoker.
 * Igual que MenuComandos del ejemplo de clase: recibe un comando con
 * setComando() y lo ejecuta con ejecutarComando(), sin saber qué hace.
 * Además lleva una bitácora de lo ejecutado.
 */
public class MenuComandos {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private IComandoEmpleado comando;
    private final List<String> bitacora = new ArrayList<>();

    public void setComando(IComandoEmpleado comando) {
        this.comando = comando;
    }

    public String ejecutarComando() throws Exception {
        if (comando == null) {
            throw new IllegalStateException("No se asignó ningún comando.");
        }
        String resultado = comando.ejecutar();
        bitacora.add(LocalTime.now().format(HORA) + "  [" + comando.getDescripcion() + "]  " + resultado);
        return resultado;
    }

    public List<String> getBitacora() {
        return new ArrayList<>(bitacora);
    }
}
