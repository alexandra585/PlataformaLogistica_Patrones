package uni.service;

/** Rastreo GPS en tiempo real con alertas inmediatas: +10 % de la tarifa. */
public class RastreoGpsDecorator extends TarifaDecorator {

    public RastreoGpsDecorator(ITarifa tarifa) {
        super(tarifa);
    }

    @Override
    public String getDescripcion() {
        return tarifa.getDescripcion() + " + Rastreo GPS";
    }

    @Override
    public double getMonto() {
        return redondear(tarifa.getMonto() * 1.10);
    }
}
