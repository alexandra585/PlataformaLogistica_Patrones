package uni.service;

import java.util.ArrayList;
import java.util.List;

/**
 * COMMAND compuesto (macro-comando): ejecuta varios comandos como una sola
 * operación. Se usa al despachar un manifiesto completo (Composite): si un
 * paquete falla, se deshacen los que ya se ejecutaron (todo o nada).
 */
public class MacroComando implements IComando {

    private final String nombre;
    private final String codigoReferencia;
    private final List<IComando> comandos = new ArrayList<>();
    private final List<IComando> ejecutados = new ArrayList<>();

    public MacroComando(String nombre, String codigoReferencia) {
        this.nombre = nombre;
        this.codigoReferencia = codigoReferencia;
    }

    public MacroComando agregar(IComando c) {
        comandos.add(c);
        return this;
    }

    @Override
    public void ejecutar() throws Exception {
        ejecutados.clear();
        for (IComando c : comandos) {
            try {
                c.ejecutar();
                ejecutados.add(c);
            } catch (Exception ex) {
                deshacer();                         // compensación de lo ya hecho
                throw new Exception("Macro \"" + nombre + "\" cancelada en "
                        + c.getCodigoEnvio() + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void deshacer() throws Exception {
        for (int i = ejecutados.size() - 1; i >= 0; i--) {
            ejecutados.get(i).deshacer();
        }
        ejecutados.clear();
    }

    public int size() {
        return comandos.size();
    }

    @Override
    public String getNombre() { return "MACRO"; }

    @Override
    public String getDescripcion() { return nombre + " (" + comandos.size() + " envíos)"; }

    @Override
    public String getCodigoEnvio() { return codigoReferencia; }

    @Override
    public String getEstadoPrevio() { return null; }

    @Override
    public String getEstadoNuevo() { return null; }

    public List<IComando> getComandos() { return comandos; }

    @Override
    public String toString() { return getDescripcion(); }
}
