package uni.util;

import java.util.HashMap;
import java.util.Map;
import uni.entity.EnvioTo;

/**
 * PATRÓN PROTOTYPE - Registro de prototipos.
 * Un cliente corporativo guarda plantillas de envío (origen, servicio,
 * peso típico, seguro...) y genera cientos de envíos clonándolas.
 */
public class RegistroPlantillas {

    private final Map<String, EnvioTo> plantillas = new HashMap<>();

    public void registrar(String clave, EnvioTo prototipo) {
        plantillas.put(clave, prototipo);
    }

    /** Devuelve una copia profunda; el prototipo nunca se modifica. */
    public EnvioTo obtener(String clave) {
        EnvioTo prototipo = plantillas.get(clave);
        if (prototipo == null) {
            throw new IllegalArgumentException("No existe la plantilla " + clave);
        }
        return prototipo.clone();
    }
}
