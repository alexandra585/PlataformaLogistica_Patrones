package uni.util;

import uni.entity.DireccionTo;
import uni.service.IGeolocalizador;

/** PATRÓN ADAPTER: adapta ExtNominatimClient a IGeolocalizador. */
public class OpenStreetMapAdapter implements IGeolocalizador {

    private final ExtNominatimClient cliente;

    public OpenStreetMapAdapter(ExtNominatimClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public DireccionTo geocodificar(DireccionTo d) {
        double[] lonLat = cliente.search(d.getCalle(), d.getDistrito(), "PE");
        d.setLongitud(lonLat[0]);                                     // se corrige el orden
        d.setLatitud(lonLat[1]);
        return d;
    }

    @Override
    public String getProveedor() {
        return "OpenStreetMap";
    }
}
