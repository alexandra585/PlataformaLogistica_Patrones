package uni.service;

/** PATRÓN DECORATOR - Decorator abstracto: envuelve otra ITarifa. */
public abstract class TarifaDecorator implements ITarifa {

    protected final ITarifa tarifa;

    protected TarifaDecorator(ITarifa tarifa) {
        this.tarifa = tarifa;
    }

    @Override
    public String getDescripcion() {
        return tarifa.getDescripcion();
    }

    @Override
    public double getMonto() {
        return tarifa.getMonto();
    }

    protected static double redondear(double monto) {
        return Math.round(monto * 100.0) / 100.0;
    }
}
