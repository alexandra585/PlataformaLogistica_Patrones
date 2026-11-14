package uni.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import uni.entity.BoletaPagoTo;
import uni.entity.EmpleadoTo;
import uni.entity.RolUsuario;
import uni.reportes.ReporteBoletaPago;
import uni.reportes.SalidaArchivoPdf;
import uni.service.DescuentoStrategy;
import uni.service.EmpleadoNomina;
import uni.service.GeneradorBoleta;
import uni.service.IRegistroBoleta;

/**
 * Pruebas por consola de las Sesiones 14 y 17 (no requieren MySQL).
 *  - antipatrones: caso Empleado de la clase, antes y después de refactorizar.
 *  - grasp: Experto en información, Polimorfismo y Fabricación pura.
 */
public class PruebaGrasp {

    public static void main(String[] args) throws Exception {
        antipatrones();
        grasp();
    }

    private static void seccion(String nombre) {
        System.out.println();
        System.out.println("##SECCION " + nombre);
    }

    /** Sesión 14: misma boleta con la versión con antipatrones y con la refactorizada. */
    static void antipatrones() {
        seccion("antipatrones");
        System.out.println("-- Parte 1: versión con antipatrones (EmpleadoAntipatron) --");
        EmpleadoAntipatron.main(new String[0]);

        System.out.println("-- Parte 2: versión refactorizada (Strategy + Factory Method + constantes) --");
        for (String tipo : new String[]{"AFP", "SNP"}) {
            EmpleadoNomina e = nomina("E001", "Roy", "Saavedra", "SISTEMAS", 2500, tipo);
            DescuentoStrategy d = DescuentoFactory.crear(tipo);
            System.out.printf("   %-3s -> %s | aporte S/ %.2f | salud S/ %.2f | horas extras (10 h) S/ %.2f"
                    + " | neto S/ %.2f%n", tipo, d.getClass().getSimpleName(), e.calcularAportePension(),
                    e.calcularDescuentoSalud(), e.calcularMontoHorasExtras(10), e.calcularSueldoNeto(10));
        }
        try {
            DescuentoFactory.crear("XYZ");
        } catch (IllegalArgumentException ex) {
            System.out.println("   Régimen desconocido: RECHAZADO -> " + ex.getMessage());
        }
    }

    /** Sesión 17: boleta de pago con los tres patrones GRASP. */
    static void grasp() throws Exception {
        seccion("grasp");
        List<String> bd = new ArrayList<>();
        IRegistroBoleta registro = new IRegistroBoleta() {      // persistencia en memoria (DIP)
            private int secuencia = 0;

            @Override
            public int registrar(BoletaPagoTo b, String usuario) {
                bd.add(String.format("boletas_pago <- %s %s neto S/ %.2f (por %s)",
                        b.getIdempleado(), b.getPeriodo(), b.getSueldoNeto(), usuario));
                return ++secuencia;
            }

            @Override
            public void cambiarRegimen(String idempleado, String regimen) {
                bd.add("empleados.regimen_pensionario <- " + idempleado + " = " + regimen);
            }
        };
        EmpleadoNomina ivan = nomina("E0010", "Iván", "Cárdenas", "REPARTO", 1600, "AFP");
        EmpleadoNomina mario = nomina("E0011", "Mario", "Huamán", "REPARTO", 1600, "SNP");

        System.out.println("-- Experto en información: EmpleadoNomina calcula con sus propios datos --");
        System.out.printf("   %s: horas extras(10) S/ %.2f, aporte S/ %.2f, salud S/ %.2f, neto S/ %.2f%n",
                ivan.getNombre(), ivan.calcularMontoHorasExtras(10), ivan.calcularAportePension(),
                ivan.calcularDescuentoSalud(), ivan.calcularSueldoNeto(10));

        System.out.println("-- Polimorfismo: el mismo mensaje calcularAportePension() --");
        for (EmpleadoNomina e : new EmpleadoNomina[]{ivan, mario}) {
            System.out.printf("   %-16s %-38s -> S/ %.2f%n", e.getNombre(), e.getRegimen().getNombre(),
                    e.calcularAportePension());
        }

        System.out.println("-- Fabricación pura: GeneradorBoleta arma, registra e imprime --");
        GeneradorBoleta generador = new GeneradorBoleta(registro);
        BoletaPagoTo b = generador.emitir(ivan, "2026-11", 10, "E0001");
        System.out.println(indentar(b.resumen()));
        ivan.setRegimen(DescuentoFactory.crear("SNP"));          // cambia el régimen en tiempo de ejecución
        BoletaPagoTo b2 = generador.emitir(ivan, "2026-12", 0, "E0001");
        System.out.printf("   Mismo empleado pasado a SNP: aporte S/ %.2f, neto S/ %.2f%n",
                b2.getAportePension(), b2.getSueldoNeto());
        intentar("   Periodo 2026-13", () -> generador.calcular(ivan, "2026-13", 0));
        intentar("   Horas extras -5", () -> generador.calcular(ivan, "2026-11", -5));
        for (String linea : bd) {
            System.out.println("   " + linea);
        }

        // Boleta en PDF con JasperReports a partir de datos en memoria (sin MySQL)
        Map<String, Object> f = new HashMap<>();
        f.put("idboleta", 1);
        f.put("idempleado", b.getIdempleado());
        f.put("periodo", b.getPeriodo());
        f.put("sueldo_basico", java.math.BigDecimal.valueOf(b.getSueldoBasico()));
        f.put("horas_extras", b.getHorasExtras());
        f.put("monto_horas_extras", java.math.BigDecimal.valueOf(b.getMontoHorasExtras()));
        f.put("regimen", b.getRegimen());
        f.put("aporte_pension", java.math.BigDecimal.valueOf(b.getAportePension()));
        f.put("descuento_salud", java.math.BigDecimal.valueOf(b.getDescuentoSalud()));
        f.put("total_ingresos", java.math.BigDecimal.valueOf(b.getTotalIngresos()));
        f.put("total_descuentos", java.math.BigDecimal.valueOf(b.getTotalDescuentos()));
        f.put("sueldo_neto", java.math.BigDecimal.valueOf(b.getSueldoNeto()));
        f.put("fecha_emision", new java.sql.Timestamp(System.currentTimeMillis()));
        f.put("nombre", "Cárdenas, Iván");
        f.put("area", "REPARTO");
        f.put("rol", "REPARTIDOR");
        f.put("sucursal", "Almacén Callao");
        List<Map<String, ?>> filas = new ArrayList<>();
        filas.add(f);
        System.out.println("   " + new ReporteBoletaPago(new SalidaArchivoPdf(
                new File("salida_reportes", "boleta_pago.pdf")), 1).generar(new JRMapCollectionDataSource(filas)));
    }

    private static EmpleadoNomina nomina(String id, String nombre, String apellidos, String area, double sueldo,
            String regimen) {
        EmpleadoTo e = new EmpleadoTo(id, nombre, apellidos, RolUsuario.REPARTIDOR);
        e.setArea(area);
        e.setSueldo(sueldo);
        e.setRegimen(regimen);
        return new EmpleadoNomina(e, (i, s, a, o, u) -> { }, "E0001");
    }

    private static String indentar(String texto) {
        return "   " + texto.replace("\n", "\n   ");
    }

    interface Accion {
        void run() throws Exception;
    }

    private static void intentar(String texto, Accion a) {
        try {
            a.run();
            System.out.println(texto + ": OK");
        } catch (Exception ex) {
            System.out.println(texto + ": RECHAZADO -> " + ex.getMessage());
        }
    }
}
