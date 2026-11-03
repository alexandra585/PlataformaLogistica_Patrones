package uni.util;

import uni.service.IFirmaDigital;

/** Producto concreto (familia Producción): firma en plataforma en la nube. */
public class FirmaDigitalCloud implements IFirmaDigital {

    @Override
    public String firmarEntrega(String codigoEnvio, String dniReceptor) {
        return "CLOUD-" + Integer.toHexString((codigoEnvio + dniReceptor).hashCode()).toUpperCase();
    }

    @Override
    public String getProveedor() {
        return "Firma electrónica en la nube";
    }
}
