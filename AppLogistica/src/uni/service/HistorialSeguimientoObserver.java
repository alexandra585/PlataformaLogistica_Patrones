package uni.service;

import uni.entity.EventoEnvio;
import uni.entity.SeguimientoTo;

/** OBSERVER concreto: agrega el registro inmutable de trazabilidad. */
public class HistorialSeguimientoObserver implements IObservadorEnvio {

    private final IRegistroHistorico<SeguimientoTo> historial;

    public HistorialSeguimientoObserver(IRegistroHistorico<SeguimientoTo> historial) {
        this.historial = historial;
    }

    @Override
    public void actualizar(EventoEnvio e) {
        try {
            historial.registrar(new SeguimientoTo(e.getCodigo(), e.getEstadoNuevo(),
                    e.getUbicacion(), e.getObservacion(), e.getIdempleado(), e.getFechahora()));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo registrar el historial: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getNombre() {
        return "Historial de seguimiento";
    }
}
