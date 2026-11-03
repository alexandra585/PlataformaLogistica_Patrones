package uni.service;

import uni.entity.EstadoEnvio;

/**
 * PATRÓN COMPOSITE - Component.
 * Un paquete individual y una agrupación (saco, paleta, manifiesto de
 * vehículo) se manipulan con las mismas operaciones.
 */
public interface IUnidadCarga {

    String getCodigo();

    double getPesoKg();

    int contarPaquetes();

    /** Cambia el estado y lo propaga a todo lo que contiene. */
    void actualizarEstado(EstadoEnvio estado);

    void mostrar(String sangria);

    /** Texto del nodo en el árbol de la vista ManifiestoView. */
    String getEtiqueta();

    /** Recorre el árbol y junta los códigos de todos los paquetes (hojas). */
    void recolectarCodigos(java.util.List<String> destino);
}
