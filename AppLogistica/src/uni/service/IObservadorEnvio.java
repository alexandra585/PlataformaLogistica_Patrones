package uni.service;

import uni.entity.EventoEnvio;

/**
 * PATRÓN OBSERVER - Observer.
 * Todo componente que deba reaccionar cuando un envío cambia de estado
 * (historial, notificación al destinatario, alertas, indicadores, pantallas)
 * implementa esta interfaz. El sujeto no conoce las clases concretas.
 */
public interface IObservadorEnvio {

    void actualizar(EventoEnvio evento);

    /** Nombre legible para la bitácora y para la pantalla de seguimiento. */
    String getNombre();
}
