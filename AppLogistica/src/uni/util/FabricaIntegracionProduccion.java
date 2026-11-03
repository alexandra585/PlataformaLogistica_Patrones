package uni.util;

import uni.service.IFabricaIntegraciones;
import uni.service.IFirmaDigital;
import uni.service.IGeolocalizador;
import uni.service.INotificador;

/** PATRÓN ABSTRACT FACTORY - Fábrica concreta: proveedores principales en la nube. */
public class FabricaIntegracionProduccion implements IFabricaIntegraciones {

    @Override
    public IGeolocalizador crearGeolocalizador() {
        return new GoogleMapsAdapter(new ExtGoogleMapsClient());
    }

    @Override
    public INotificador crearNotificador() {
        return new WhatsAppAdapter(new ExtWhatsAppBusinessApi());
    }

    @Override
    public IFirmaDigital crearFirmaDigital() {
        return new FirmaDigitalCloud();
    }

    @Override
    public String getNombreFamilia() {
        return "PRODUCCIÓN (nube)";
    }
}
