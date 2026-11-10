package uni.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PATRÓN MEMENTO (Sesión 13) - Memento.
 * Fotografía inmutable del sueldo y el área de un empleado. Solo el
 * Originator (EmpleadoNomina) lee su contenido para restaurarse; el
 * Caretaker (HistorialEmpleado) únicamente lo guarda.
 */
public final class EmpleadoMemento {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String idempleado;
    private final double sueldo;
    private final String area;
    private final String etiqueta;
    private final LocalDateTime fecha = LocalDateTime.now();

    EmpleadoMemento(String idempleado, double sueldo, String area, String etiqueta) {
        this.idempleado = idempleado;
        this.sueldo = sueldo;
        this.area = area;
        this.etiqueta = etiqueta;
    }

    // Visibilidad de paquete: solo el Originator (mismo paquete) accede al estado.
    double getSueldo() { return sueldo; }
    String getArea() { return area; }

    public String getIdempleado() { return idempleado; }
    public String getEtiqueta() { return etiqueta; }
    public LocalDateTime getFecha() { return fecha; }

    @Override
    public String toString() {
        return String.format("%s  %s  (S/ %.2f - %s)", fecha.format(HORA), etiqueta, sueldo, area);
    }
}
