package uni.controller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import uni.entity.ComandoLogTo;
import uni.service.IBitacoraComandos;
import uni.service.IComando;
import uni.service.MacroComando;
import uni.util.SesionUsuario;

/**
 * PATRÓN COMMAND - Invoker.
 * Los formularios no llaman directamente al servicio: crean un comando y
 * se lo entregan al gestor, que lo ejecuta, guarda la pila de deshacer y
 * rehacer, mantiene la cola de comandos capturados sin conexión (app móvil
 * del repartidor) y escribe la bitácora en comandos_log.
 */
public class GestorComandos {

    private final Deque<IComando> pilaDeshacer = new ArrayDeque<>();
    private final Deque<IComando> pilaRehacer = new ArrayDeque<>();
    private final Deque<IComando> pendientes = new ArrayDeque<>();
    private final Map<IComando, Long> idsBitacora = new IdentityHashMap<>();
    private final IBitacoraComandos bitacora;     // null = sin persistencia (pruebas)

    public GestorComandos(IBitacoraComandos bitacora) {
        this.bitacora = bitacora;
    }

    public void ejecutar(IComando c) throws Exception {
        c.ejecutar();
        pilaDeshacer.push(c);
        pilaRehacer.clear();
        registrar(c);
    }

    public IComando deshacer() throws Exception {
        if (pilaDeshacer.isEmpty()) {
            throw new IllegalStateException("No hay acciones para deshacer.");
        }
        IComando c = pilaDeshacer.peek();
        c.deshacer();                     // si falla (p. ej. Proxy lo niega) queda en la pila
        pilaDeshacer.pop();
        pilaRehacer.push(c);
        marcar(c, true);
        return c;
    }

    public IComando rehacer() throws Exception {
        if (pilaRehacer.isEmpty()) {
            throw new IllegalStateException("No hay acciones para rehacer.");
        }
        IComando c = pilaRehacer.peek();
        c.ejecutar();
        pilaRehacer.pop();
        pilaDeshacer.push(c);
        registrar(c);
        return c;
    }

    /** Modo sin conexión: el comando se guarda y se ejecuta al sincronizar. */
    public void encolar(IComando c) {
        pendientes.addLast(c);
    }

    /** Ejecuta en orden los comandos pendientes; devuelve el resultado de cada uno. */
    public List<String> sincronizar() {
        List<String> resultado = new ArrayList<>();
        while (!pendientes.isEmpty()) {
            IComando c = pendientes.pollFirst();
            try {
                ejecutar(c);
                resultado.add("OK    " + c.getDescripcion());
            } catch (Exception ex) {
                resultado.add("ERROR " + c.getDescripcion() + " -> " + ex.getMessage());
            }
        }
        return resultado;
    }

    public boolean puedeDeshacer() { return !pilaDeshacer.isEmpty(); }

    public boolean puedeRehacer() { return !pilaRehacer.isEmpty(); }

    public List<IComando> getHistorial() { return new ArrayList<>(pilaDeshacer); }

    public List<IComando> getPendientes() { return new ArrayList<>(pendientes); }

    public String getSiguienteDeshacer() {
        return pilaDeshacer.isEmpty() ? "-" : pilaDeshacer.peek().getDescripcion();
    }

    // ----------------------------------------------------------- bitácora
    private void registrar(IComando c) {
        if (bitacora == null) {
            return;
        }
        List<IComando> lista = new ArrayList<>();
        if (c instanceof MacroComando) {
            lista.addAll(((MacroComando) c).getComandos());
        } else {
            lista.add(c);
        }
        String usuario = SesionUsuario.getInstancia().getUsuario().getIdempleado();
        for (IComando x : lista) {
            try {
                long id = bitacora.registrar(new ComandoLogTo(x.getCodigoEnvio(), x.getNombre(),
                        x.getDescripcion(), x.getEstadoPrevio(), x.getEstadoNuevo(), usuario));
                idsBitacora.put(x, id);
            } catch (Exception ex) {
                System.out.println("[Bitácora] " + ex.getMessage());
            }
        }
    }

    private void marcar(IComando c, boolean deshecho) {
        if (bitacora == null) {
            return;
        }
        List<IComando> lista = new ArrayList<>();
        if (c instanceof MacroComando) {
            lista.addAll(((MacroComando) c).getComandos());
        } else {
            lista.add(c);
        }
        for (IComando x : lista) {
            Long id = idsBitacora.get(x);
            if (id != null) {
                try {
                    bitacora.marcarDeshecho(id, deshecho);
                } catch (Exception ex) {
                    System.out.println("[Bitácora] " + ex.getMessage());
                }
            }
        }
    }
}
