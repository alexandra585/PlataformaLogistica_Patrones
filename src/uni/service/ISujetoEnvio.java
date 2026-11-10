package uni.service;

import uni.entity.EventoEnvio;

/** PATRÓN OBSERVER - Subject: administra la lista de observadores. */
public interface ISujetoEnvio {

    void suscribir(IObservadorEnvio observador);

    void desuscribir(IObservadorEnvio observador);

    void notificar(EventoEnvio evento);
}
