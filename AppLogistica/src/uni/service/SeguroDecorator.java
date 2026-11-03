package uni.service;

/** Seguro contra pérdida: 2 % del valor declarado (mínimo S/ 5.00). */
public class SeguroDecorator extends TarifaDecorator {

    private final double valorDeclarado;

    public SeguroDecorator(ITarifa tarifa, double valorDeclarado) {
        super(tarifa);
        this.valorDeclarado = valorDeclarado;
    }

    @Override
    public String getDescripcion() {
        return tarifa.getDescripcion() + " + Seguro";
    }

    @Override
    public double getMonto() {
        return redondear(tarifa.getMonto() + Math.max(5.00, valorDeclarado * 0.02));
    }
}
