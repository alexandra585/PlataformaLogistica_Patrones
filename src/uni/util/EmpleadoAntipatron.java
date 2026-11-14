package uni.util;

/**
 * SOLO DIDÁCTICO - Sesión 14 (antipatrones). NO se usa en la aplicación.
 * Reproduce la "Versión con antipatrones" del caso Empleado de la clase para
 * contrastarla con la versión refactorizada (EmpleadoNomina + DescuentoStrategy
 * + DescuentoFactory + GeneradorBoleta). Antipatrones presentes a propósito:
 * GOD OBJECT, SPAGHETTI CODE, MAGIC NUMBERS, COPY-PASTE PROGRAMMING,
 * falta de encapsulamiento y responsabilidad múltiple.
 */
public class EmpleadoAntipatron {

    // Atributos sin encapsulación
    public String codigo;
    public String nombres;
    public String area;
    public double sueldo;
    public int horasExtras;
    public String afp;

    // Cálculos, reglas e impresión mezclados en un solo método
    public void mostrarBoleta() {
        double descuentoAFP = 0;
        double descuentoSNP = 0;
        double descuentoSalud = 0.03 * sueldo;
        double montoHorasExtras = sueldo * horasExtras / 240;

        // Magic numbers y condicionales rígidos
        if (afp.equals("AFP")) {
            descuentoAFP = 0.11 * sueldo;
        } else if (afp.equals("SNP")) {
            descuentoSNP = 0.06 * sueldo;
        }

        double totalDescuento = descuentoAFP + descuentoSNP + descuentoSalud;
        double sueldoFinal = (sueldo + montoHorasExtras) - totalDescuento;

        System.out.println("=== BOLETA DE PAGO ===");
        System.out.println("Código: " + codigo);
        System.out.println("Nombre: " + nombres);
        System.out.println("Área: " + area);
        System.out.println("Sueldo básico: " + sueldo);
        System.out.println("Horas extras: " + horasExtras);
        System.out.println("Descuento AFP: " + descuentoAFP);
        System.out.println("Descuento SNP: " + descuentoSNP);
        System.out.println("Descuento Salud: " + descuentoSalud);
        System.out.println("Sueldo neto: " + sueldoFinal);
    }

    // Método main dentro de la misma clase (mala práctica)
    public static void main(String[] args) {
        EmpleadoAntipatron e = new EmpleadoAntipatron();
        e.codigo = "E001";
        e.nombres = "Roy Saavedra";
        e.area = "Sistemas";
        e.sueldo = 2500;
        e.horasExtras = 10;
        e.afp = "AFP";
        e.mostrarBoleta();
    }
}
