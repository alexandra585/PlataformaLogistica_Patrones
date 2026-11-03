package uni.util;

/**
 * ADAPTEE (simula el cliente de geocodificación de Google Maps).
 * Recibe una línea de texto y devuelve "latitud,longitud".
 */
public class ExtGoogleMapsClient {

    public String geocode(String addressLine) {
        int h = Math.abs(addressLine.hashCode());
        double lat = -12.0 - (h % 1000) / 10000.0;
        double lng = -77.0 - (h % 700) / 10000.0;
        return String.format(java.util.Locale.US, "%.5f,%.5f", lat, lng);
    }
}
