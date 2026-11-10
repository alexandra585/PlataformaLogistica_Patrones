package uni.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import uni.entity.AccionEnvio;
import uni.util.TransicionInvalidaException;

/**
 * PATRÓN STATE - comportamiento por defecto: toda acción está prohibida.
 * Cada estado concreto sobrescribe solo las acciones que sí admite.
 */
public abstract class EstadoEnvioBase implements IEstadoEnvio {

    protected void prohibir(AccionEnvio accion) {
        throw new TransicionInvalidaException(getCodigo(), accion);
    }

    @Override
    public void recoger(EnvioContexto c) { prohibir(AccionEnvio.RECOGER); }

    @Override
    public void trasladar(EnvioContexto c) { prohibir(AccionEnvio.TRASLADAR); }

    @Override
    public void clasificar(EnvioContexto c) { prohibir(AccionEnvio.CLASIFICAR); }

    @Override
    public void despachar(EnvioContexto c) { prohibir(AccionEnvio.DESPACHAR); }

    @Override
    public void entregar(EnvioContexto c) { prohibir(AccionEnvio.ENTREGAR); }

    @Override
    public void observar(EnvioContexto c) { prohibir(AccionEnvio.OBSERVAR); }

    @Override
    public void reprogramar(EnvioContexto c, LocalDate nuevaFecha) { prohibir(AccionEnvio.REPROGRAMAR); }

    @Override
    public void devolver(EnvioContexto c) { prohibir(AccionEnvio.DEVOLVER); }

    @Override
    public List<AccionEnvio> accionesPermitidas(EnvioContexto c) {
        return new ArrayList<>();
    }

    @Override
    public boolean esFinal() {
        return false;
    }

    /** Agrega a la lista las acciones de avance que el flujo del servicio permite. */
    protected void agregarSiFlujoPermite(EnvioContexto c, List<AccionEnvio> lista, AccionEnvio... acciones) {
        for (AccionEnvio a : acciones) {
            if (c.flujoPermite(a.getEstadoDestino())) {
                lista.add(a);
            }
        }
    }

    @Override
    public String toString() {
        return getCodigo().name();
    }
}
