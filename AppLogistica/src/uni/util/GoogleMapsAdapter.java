package uni.util;

import uni.entity.DireccionTo;
import uni.service.IGeolocalizador;

/** PATRÓN ADAPTER: adapta ExtGoogleMapsClient a IGeolocalizador. */
public class GoogleMapsAdapter implements IGeolocalizador {

    private final ExtGoogleMapsClient cliente;

    public GoogleMapsAdapter(ExtGoogleMapsClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public DireccionTo geocodificar(DireccionTo d) {
        String linea = d.getCalle() + ", " + d.getDistrito() + ", " + d.getProvincia() + ", Peru";
        String[] partes = cliente.geocode(linea).split(",");          // "lat,lng"
        d.setLatitud(Double.parseDouble(partes[0]));
        d.setLongitud(Double.parseDouble(partes[1]));
        return d;
    }

    @Override
    public String getProveedor() {
        return "Google Maps";
    }
}
