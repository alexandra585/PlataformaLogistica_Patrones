package uni.service;

import uni.entity.EnvioTo;
import uni.entity.EventoEnvio;

/** OBSERVER concreto: guarda en la BD el nuevo estado del envío. */
public class PersistenciaEnvioObserver implements IObservadorEnvio {

    private final IEscritura<EnvioTo> envioDao;

    public PersistenciaEnvioObserver(IEscritura<EnvioTo> envioDao) {
        this.envioDao = envioDao;
    }

    @Override
    public void actualizar(EventoEnvio evento) {
        try {
            envioDao.update(evento.getEnvio());
        } catch (Exception ex) {
            // Sin persistencia el cambio no es válido: se propaga el error.
            throw new IllegalStateException("No se pudo guardar el estado: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getNombre() {
        return "Persistencia del envío";
    }
}
