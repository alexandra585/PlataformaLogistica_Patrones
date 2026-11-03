package uni.service;

import uni.entity.EnvioTo;

/**
 * PATRÓN COMMAND - comando abstracto con deshacer basado en MEMENTO.
 * Antes de ejecutar pide al receptor (ISeguimientoService) un punto de
 * restauración; deshacer() le entrega ese memento para volver atrás.
 * El comando actúa como Caretaker: guarda el memento sin leer su contenido.
 */
public abstract class ComandoEnvioBase implements IComando {

    protected final ISeguimientoService receptor;
    protected final String codigoEnvio;
    private EnvioMemento respaldo;
    private String estadoPrevio;
    private String estadoNuevo;

    protected ComandoEnvioBase(ISeguimientoService receptor, String codigoEnvio) {
        this.receptor = receptor;
        this.codigoEnvio = codigoEnvio;
    }

    /** Operación concreta sobre el receptor. */
    protected abstract void accion() throws Exception;

    @Override
    public final void ejecutar() throws Exception {
        respaldo = receptor.crearPuntoRestauracion(codigoEnvio, getNombre());
        estadoPrevio = estadoActual();
        accion();
        estadoNuevo = estadoActual();
    }

    @Override
    public void deshacer() throws Exception {
        if (respaldo == null) {
            throw new IllegalStateException("El comando aún no se ha ejecutado.");
        }
        receptor.restaurar(codigoEnvio, respaldo, "deshacer \"" + getDescripcion() + "\"");
    }

    private String estadoActual() throws Exception {
        EnvioTo e = receptor.buscarEnvio(codigoEnvio);
        return e == null ? null : e.getEstado().name();
    }

    @Override
    public String getCodigoEnvio() { return codigoEnvio; }

    @Override
    public String getEstadoPrevio() { return estadoPrevio; }

    @Override
    public String getEstadoNuevo() { return estadoNuevo; }

    @Override
    public String toString() {
        return getDescripcion();
    }
}
