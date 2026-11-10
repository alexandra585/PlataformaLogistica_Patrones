package uni.service;

import uni.entity.AlertaTo;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.EventoEnvio;
import uni.entity.TipoServicio;

/**
 * OBSERVER concreto: genera alertas para el supervisor de ruta cuando el
 * cambio de estado requiere intervención humana.
 */
public class AlertaSupervisorObserver implements IObservadorEnvio {

    private final IRegistroHistorico<AlertaTo> alertas;

    public AlertaSupervisorObserver(IRegistroHistorico<AlertaTo> alertas) {
        this.alertas = alertas;
    }

    @Override
    public void actualizar(EventoEnvio e) {
        AlertaTo alerta = evaluar(e);
        if (alerta == null) {
            return;
        }
        try {
            alertas.registrar(alerta);
        } catch (Exception ex) {
            System.out.println("[Alerta] no se pudo registrar: " + ex.getMessage());
        }
    }

    /** Reglas de negocio de escalamiento (público para pruebas). */
    public AlertaTo evaluar(EventoEnvio e) {
        EnvioTo envio = e.getEnvio();
        if (e.isReversion()) {
            return e.getEstadoAnterior() == EstadoEnvio.ENTREGADO
                    ? new AlertaTo(envio.getCodigo(), "ALTA", "Se revirtió una entrega: " + e.getObservacion())
                    : null;
        }
        switch (e.getEstadoNuevo()) {
            case OBSERVADO:
                if (envio.getServicio() == TipoServicio.EXPRESS) {
                    return new AlertaTo(envio.getCodigo(), "CRITICA",
                            "Envío EXPRESS observado: riesgo de incumplir 24 h. " + texto(e));
                }
                if (envio.getIntentos() >= EstadoObservado.MAX_INTENTOS) {
                    return new AlertaTo(envio.getCodigo(), "CRITICA",
                            "Tercer intento fallido: proceder con la devolución. " + texto(e));
                }
                return new AlertaTo(envio.getCodigo(), "ALTA", "Envío observado (intento "
                        + envio.getIntentos() + "/" + EstadoObservado.MAX_INTENTOS + "): " + texto(e));
            case DEVUELTO:
                return new AlertaTo(envio.getCodigo(), "ALTA", "Envío devuelto al remitente.");
            default:
                return null;
        }
    }

    private String texto(EventoEnvio e) {
        return e.getObservacion() == null ? "" : e.getObservacion();
    }

    @Override
    public String getNombre() {
        return "Alertas a supervisores";
    }
}
