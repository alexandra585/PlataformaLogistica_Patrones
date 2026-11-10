package uni.service;

import uni.entity.EmpleadoTo;

/**
 * Sesión 13 - un solo objeto con dos roles:
 *  - RECEIVER del patrón Command: sabe realizar las acciones reales
 *    (mostrar datos, calcular bono, sueldo final, aumentar sueldo, cambiar área).
 *  - ORIGINATOR del patrón Memento: crea mementos de su estado
 *    (sueldo + área) y se restaura a partir de ellos.
 * Equivale a la clase Empleado / EmpleadoOriginator del ejemplo de clase,
 * pero persiste los cambios en MySQL mediante IRegistroRemuneracion.
 */
public class EmpleadoNomina {

    public static final double PORCENTAJE_BONO = 0.10;

    private final String idempleado;
    private final String nombre;
    private double sueldo;
    private String area;
    private final IRegistroRemuneracion registro;
    private final String usuario;

    public EmpleadoNomina(EmpleadoTo e, IRegistroRemuneracion registro, String usuario) {
        this.idempleado = e.getIdempleado();
        this.nombre = e.getNombreCompleto();
        this.sueldo = e.getSueldo();
        this.area = e.getArea();
        this.registro = registro;
        this.usuario = usuario;
    }

    // ------------------------------------------------ Receiver (Command)

    public String mostrarDatos() {
        return String.format("Empleado: %s - %s | Área: %s | Sueldo: S/ %.2f", idempleado, nombre, area, sueldo);
    }

    public double calcularBono() {
        return redondear(sueldo * PORCENTAJE_BONO);
    }

    public double calcularSueldoFinal() {
        return redondear(sueldo + calcularBono());
    }

    public void aumentarSueldo(double porcentaje) throws Exception {
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje de aumento debe estar entre 0 y 100.");
        }
        aplicar(redondear(sueldo * (1 + porcentaje / 100.0)), area, "AUMENTO");
    }

    public void cambiarArea(String nuevaArea) throws Exception {
        if (nuevaArea == null || nuevaArea.trim().isEmpty()) {
            throw new IllegalArgumentException("Indique el área.");
        }
        aplicar(sueldo, nuevaArea.trim().toUpperCase(), "CAMBIO_AREA");
    }

    // ------------------------------------------------ Originator (Memento)

    public EmpleadoMemento guardar(String etiqueta) {
        return new EmpleadoMemento(idempleado, sueldo, area, etiqueta);
    }

    public void restaurar(EmpleadoMemento m) throws Exception {
        if (!m.getIdempleado().equals(idempleado)) {
            throw new IllegalArgumentException("El punto de restauración pertenece a otro empleado.");
        }
        aplicar(m.getSueldo(), m.getArea(), "RESTAURACION");
    }

    // ------------------------------------------------ apoyo

    private void aplicar(double nuevoSueldo, String nuevaArea, String operacion) throws Exception {
        registro.actualizarRemuneracion(idempleado, nuevoSueldo, nuevaArea, operacion, usuario);
        this.sueldo = nuevoSueldo;   // solo cambia en memoria si la BD lo aceptó
        this.area = nuevaArea;
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public String getIdempleado() { return idempleado; }
    public String getNombre() { return nombre; }
    public double getSueldo() { return sueldo; }
    public String getArea() { return area; }
}
