package uni.service;

/**
 * PATRÓN ABSTRACT FACTORY.
 * Crea una familia coherente de integraciones externas
 * (geolocalización + notificación + firma digital).
 */
public interface IFabricaIntegraciones {

    IGeolocalizador crearGeolocalizador();

    INotificador crearNotificador();

    IFirmaDigital crearFirmaDigital();

    String getNombreFamilia();
}
