package uni.util;

import uni.service.IFirmaDigital;

/** Producto concreto (familia Contingencia): firma capturada en el móvil, sin internet. */
public class FirmaDigitalLocal implements IFirmaDigital {

    @Override
    public String firmarEntrega(String codigoEnvio, String dniReceptor) {
        return "LOCAL-" + Math.abs((codigoEnvio + dniReceptor).hashCode() % 100000);
    }

    @Override
    public String getProveedor() {
        return "Firma local en dispositivo (sincroniza luego)";
    }
}
