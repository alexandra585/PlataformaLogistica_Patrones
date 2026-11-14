package uni.service;

import uni.entity.EmpleadoTo;
import uni.util.DescuentoFactory;
import uni.util.ParametrosPlanilla;

/**
 * Sesión 13 - un solo objeto con dos roles:
 *  - RECEIVER del patrón Command: sabe realizar las acciones reales
 *    (mostrar datos, calcular bono, sueldo final, aumentar sueldo, cambiar área).
 *  - ORIGINATOR del patrón Memento: crea mementos de su estado
 *    (sueldo + área) y se restaura a partir de ellos.
 * Equivale a la clase Empleado / EmpleadoOriginator del ejemplo de clase,
 * pero persiste los cambios en MySQL mediante IRegistroRemuneracion.
 *
 * Sesión 17 - GRASP EXPERTO EN INFORMACIÓN: como conoce el sueldo y el régimen
 * pensionario del empleado, es la clase responsable de calcular las horas
 * extras, los descuentos y el sueldo neto. El descuento de pensión lo delega
 * polimórficamente en su DescuentoStrategy (AFP o SNP).
 */
public class EmpleadoNomina {

    public static final double PORCENTAJE_BONO = 0.10;

    private final String idempleado;
    private final String nombre;
    private double sueldo;
    private String area;
    private final IRegistroRemuneracion registro;
    private final String usuario;
    private DescuentoStrategy regimen;   // Polimorfismo (AFP | SNP)

    public EmpleadoNomina(EmpleadoTo e, IRegistroRemuneracion registro, String usuario) {
        this.idempleado = e.getIdempleado();
        this.nombre = e.getNombreCompleto();
        this.sueldo = e.getSueldo();
        this.area = e.getArea();
        this.registro = registro;
        this.usuario = usuario;
        this.regimen = DescuentoFactory.crear(e.getRegimen() == null ? "AFP" : e.getRegimen());
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

    // ------------------------------------------------ Experto en información (GRASP, Sesión 17)

    /** Monto de horas extras = sueldo básico x horas / 240 (fórmula del caso de clase). */
    public double calcularMontoHorasExtras(int horas) {
        if (horas < 0) {
            throw new IllegalArgumentException("Las horas extras no pueden ser negativas.");
        }
        return redondear(sueldo * horas / ParametrosPlanilla.HORAS_MES);
    }

    /** Aporte pensionario: lo calcula la estrategia del régimen (polimorfismo). */
    public double calcularAportePension() {
        return regimen.calcularDescuento(sueldo);
    }

    public double calcularDescuentoSalud() {
        return redondear(sueldo * ParametrosPlanilla.TASA_SALUD);
    }

    public double calcularTotalDescuentos() {
        return redondear(calcularAportePension() + calcularDescuentoSalud());
    }

    public double calcularSueldoNeto(int horas) {
        return redondear(sueldo + calcularMontoHorasExtras(horas) - calcularTotalDescuentos());
    }

    public DescuentoStrategy getRegimen() {
        return regimen;
    }

    public void setRegimen(DescuentoStrategy regimen) {
        this.regimen = regimen;
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
