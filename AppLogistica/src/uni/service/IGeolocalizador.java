package uni.service;

import uni.entity.DireccionTo;

/** Producto abstracto (Abstract Factory): servicio de geolocalización. */
public interface IGeolocalizador {

    /** Completa latitud y longitud de la dirección. */
    DireccionTo geocodificar(DireccionTo direccion);

    String getProveedor();
}
