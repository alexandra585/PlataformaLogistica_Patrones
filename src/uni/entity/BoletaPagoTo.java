package uni.entity;

/** Transfer Object de la boleta de pago (tabla boletas_pago). */
public class BoletaPagoTo {

    private int idboleta;
    private String idempleado;
    private String nombre;
    private String area;
    private String periodo;
    private double sueldoBasico;
    private int horasExtras;
    private double montoHorasExtras;
    private String regimen;
    private double aportePension;
    private double descuentoSalud;
    private double totalIngresos;
    private double totalDescuentos;
    private double sueldoNeto;

    public int getIdboleta() { return idboleta; }
    public void setIdboleta(int idboleta) { this.idboleta = idboleta; }
    public String getIdempleado() { return idempleado; }
    public void setIdempleado(String idempleado) { this.idempleado = idempleado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public double getSueldoBasico() { return sueldoBasico; }
    public void setSueldoBasico(double sueldoBasico) { this.sueldoBasico = sueldoBasico; }
    public int getHorasExtras() { return horasExtras; }
    public void setHorasExtras(int horasExtras) { this.horasExtras = horasExtras; }
    public double getMontoHorasExtras() { return montoHorasExtras; }
    public void setMontoHorasExtras(double montoHorasExtras) { this.montoHorasExtras = montoHorasExtras; }
    public String getRegimen() { return regimen; }
    public void setRegimen(String regimen) { this.regimen = regimen; }
    public double getAportePension() { return aportePension; }
    public void setAportePension(double aportePension) { this.aportePension = aportePension; }
    public double getDescuentoSalud() { return descuentoSalud; }
    public void setDescuentoSalud(double descuentoSalud) { this.descuentoSalud = descuentoSalud; }
    public double getTotalIngresos() { return totalIngresos; }
    public void setTotalIngresos(double totalIngresos) { this.totalIngresos = totalIngresos; }
    public double getTotalDescuentos() { return totalDescuentos; }
    public void setTotalDescuentos(double totalDescuentos) { this.totalDescuentos = totalDescuentos; }
    public double getSueldoNeto() { return sueldoNeto; }
    public void setSueldoNeto(double sueldoNeto) { this.sueldoNeto = sueldoNeto; }

    /** Texto de la boleta para la vista previa (la presentación no la hace el Experto). */
    public String resumen() {
        return String.format("BOLETA DE PAGO %s%n%s - %s (%s)%n%n"
                + "Sueldo básico .................. S/ %10.2f%n"
                + "Horas extras (%2d h) ............ S/ %10.2f%n"
                + "TOTAL INGRESOS ................. S/ %10.2f%n%n"
                + "Aporte %-3s ...................... S/ %10.2f%n"
                + "Salud (3 %%) ..................... S/ %10.2f%n"
                + "TOTAL DESCUENTOS ............... S/ %10.2f%n%n"
                + "SUELDO NETO .................... S/ %10.2f",
                periodo, idempleado, nombre, area, sueldoBasico, horasExtras, montoHorasExtras, totalIngresos,
                regimen, aportePension, descuentoSalud, totalDescuentos, sueldoNeto);
    }
}
