package uni.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import uni.service.EmpleadoMemento;

/**
 * PATRÓN MEMENTO (Sesión 13) - Caretaker.
 * Equivale a la clase Historial del ejemplo de clase: guarda los mementos en
 * una pila y los devuelve para deshacer, sin conocer ni modificar su contenido.
 */
public class HistorialEmpleado {

    private final Deque<EmpleadoMemento> pila = new ArrayDeque<>();

    public void guardar(EmpleadoMemento m) {
        pila.push(m);
    }

    /** Devuelve (y retira) el último estado guardado, o null si no hay. */
    public EmpleadoMemento deshacer() {
        return pila.isEmpty() ? null : pila.pop();
    }

    /** Retira un memento concreto (cuando se restaura uno intermedio). */
    public void descartar(EmpleadoMemento m) {
        pila.remove(m);
    }

    /** Del más reciente al más antiguo. */
    public List<EmpleadoMemento> listar() {
        return new ArrayList<>(pila);
    }

    public boolean estaVacio() {
        return pila.isEmpty();
    }
}
