package uni.util;

import uni.service.IFabricaIntegraciones;
import uni.service.IFirmaDigital;
import uni.service.IGeolocalizador;
import uni.service.INotificador;

/**
 * PATRÓN ABSTRACT FACTORY - Fábrica concreta: proveedores alternos para
 * continuidad operativa (caída de proveedor principal o zonas sin datos).
 */
public class FabricaIntegracionContingencia implements IFabricaIntegraciones {

    @Override
    public IGeolocalizador crearGeolocalizador() {
        return new OpenStreetMapAdapter(new ExtNominatimClient());
    }

    @Override
    public INotificador crearNotificador() {
        return new SmsAdapter(new ExtSmsGatewayLegacy());
    }

    @Override
    public IFirmaDigital crearFirmaDigital() {
        return new FirmaDigitalLocal();
    }

    @Override
    public String getNombreFamilia() {
        return "CONTINGENCIA (proveedores alternos)";
    }
}
