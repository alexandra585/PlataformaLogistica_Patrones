package uni.util;

/**
 * ADAPTEE (simula el cliente de OpenStreetMap / Nominatim).
 * Recibe los campos por separado y devuelve {longitud, latitud}
 * (orden inverso al que usa el sistema).
 */
public class ExtNominatimClient {

    public double[] search(String street, String city, String country) {
        int h = Math.abs((street + city).hashCode());
        double lat = -12.0 - (h % 1000) / 10000.0;
        double lon = -77.0 - (h % 700) / 10000.0;
        return new double[]{lon, lat};
    }
}
