package uni.util;

/**
 * Sesión 14 - elimina el antipatrón MAGIC NUMBERS: los valores comunes para
 * todos los empleados (porcentajes de ley y horas del mes) tienen nombre y
 * están en un solo lugar.
 */
public final class ParametrosPlanilla {

    /** Aporte obligatorio al sistema privado de pensiones (AFP). */
    public static final double TASA_AFP = 0.11;
    /** Aporte al sistema nacional de pensiones (SNP / ONP). */
    public static final double TASA_SNP = 0.06;
    /** Descuento por salud. */
    public static final double TASA_SALUD = 0.03;
    /** Horas laborables del mes para el valor de la hora extra. */
    public static final int HORAS_MES = 240;

    private ParametrosPlanilla() {
    }
}
