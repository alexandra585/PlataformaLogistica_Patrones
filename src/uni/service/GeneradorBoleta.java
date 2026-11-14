package uni.service;

import uni.entity.BoletaPagoTo;
import uni.reportes.ISalidaReporte;
import uni.reportes.ReporteBoletaPago;

/**
 * GRASP FABRICACIÓN PURA (Sesión 17).
 * "Generar una boleta" no es responsabilidad natural de ningún objeto del
 * dominio: si la tuviera EmpleadoNomina, mezclaría cálculos con persistencia e
 * impresión (el God Object del caso de antipatrones). Por eso se crea esta
 * clase artificial, muy cohesiva, que:
 *   1) pide los montos al EXPERTO (EmpleadoNomina),
 *   2) arma la BoletaPagoTo,
 *   3) la registra mediante IRegistroBoleta (DIP),
 *   4) la entrega como reporte JasperReports usando el Bridge existente.
 */
public class GeneradorBoleta {

    private final IRegistroBoleta registro;

    public GeneradorBoleta(IRegistroBoleta registro) {
        this.registro = registro;
    }

    /** Calcula la boleta sin guardarla (vista previa). */
    public BoletaPagoTo calcular(EmpleadoNomina empleado, String periodo, int horasExtras) {
        if (periodo == null || !periodo.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new IllegalArgumentException("El periodo debe tener el formato AAAA-MM (por ejemplo 2026-11).");
        }
        BoletaPagoTo b = new BoletaPagoTo();
        b.setIdempleado(empleado.getIdempleado());
        b.setNombre(empleado.getNombre());
        b.setArea(empleado.getArea());
        b.setPeriodo(periodo);
        b.setSueldoBasico(empleado.getSueldo());
        b.setHorasExtras(horasExtras);
        b.setMontoHorasExtras(empleado.calcularMontoHorasExtras(horasExtras));     // Experto
        b.setRegimen(empleado.getRegimen().getCodigo());
        b.setAportePension(empleado.calcularAportePension());                       // Polimorfismo
        b.setDescuentoSalud(empleado.calcularDescuentoSalud());
        b.setTotalIngresos(redondear(b.getSueldoBasico() + b.getMontoHorasExtras()));
        b.setTotalDescuentos(empleado.calcularTotalDescuentos());
        b.setSueldoNeto(empleado.calcularSueldoNeto(horasExtras));
        return b;
    }

    /** Calcula, guarda el régimen elegido y registra la boleta del periodo. */
    public BoletaPagoTo emitir(EmpleadoNomina empleado, String periodo, int horasExtras, String usuario)
            throws Exception {
        BoletaPagoTo b = calcular(empleado, periodo, horasExtras);
        registro.cambiarRegimen(empleado.getIdempleado(), b.getRegimen());
        b.setIdboleta(registro.registrar(b, usuario));
        return b;
    }

    /** Entrega la boleta como PDF / visor / impresora (Bridge de JasperReports). */
    public String imprimir(BoletaPagoTo boleta, ISalidaReporte salida) throws Exception {
        return new ReporteBoletaPago(salida, boleta.getIdboleta()).generar();
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
