package uni.service;

/**
 * Sesión 14: patrón STRATEGY del ejemplo de clase (cálculo del descuento según
 * AFP o SNP). Sesión 17: es también el patrón GRASP POLIMORFISMO, porque la
 * variación por tipo de régimen se resuelve con clases que responden de forma
 * distinta al mismo mensaje, sin if/else sobre un texto.
 */
public interface DescuentoStrategy {

    /** Código guardado en empleados.regimen_pensionario (AFP | SNP). */
    String getCodigo();

    String getNombre();

    double getTasa();

    default double calcularDescuento(double sueldo) {
        return Math.round(sueldo * getTasa() * 100.0) / 100.0;
    }
}
