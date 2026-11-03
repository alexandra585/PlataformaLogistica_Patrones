package uni.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uni.service.EnvioMemento;

/**
 * PATRÓN MEMENTO - Caretaker.
 * Guarda puntos de restauración por envío (por ejemplo, antes de una
 * reprogramación solicitada por el cliente). No puede leer ni modificar
 * el contenido de los mementos: solo los almacena y los devuelve.
 */
public class HistorialMementos {

    private static final int MAXIMO_POR_ENVIO = 10;
    private final Map<String, Deque<EnvioMemento>> puntos = new HashMap<>();

    public void guardar(EnvioMemento m) {
        Deque<EnvioMemento> pila = puntos.computeIfAbsent(m.getCodigoEnvio(), k -> new ArrayDeque<>());
        pila.push(m);
        if (pila.size() > MAXIMO_POR_ENVIO) {
            pila.removeLast();                      // descarta el más antiguo
        }
    }

    public List<EnvioMemento> listar(String codigoEnvio) {
        return new ArrayList<>(puntos.getOrDefault(codigoEnvio, new ArrayDeque<>()));
    }

    public EnvioMemento ultimo(String codigoEnvio) {
        Deque<EnvioMemento> pila = puntos.get(codigoEnvio);
        return pila == null ? null : pila.peek();
    }

    public void descartar(EnvioMemento m) {
        Deque<EnvioMemento> pila = puntos.get(m.getCodigoEnvio());
        if (pila != null) {
            pila.remove(m);
        }
    }
}
