package uni.service;

/** Confirmación de entrega con firma digital del receptor: S/ 4.50. */
public class FirmaDigitalDecorator extends TarifaDecorator {

    public FirmaDigitalDecorator(ITarifa tarifa) {
        super(tarifa);
    }

    @Override
    public String getDescripcion() {
        return tarifa.getDescripcion() + " + Firma digital";
    }

    @Override
    public double getMonto() {
        return redondear(tarifa.getMonto() + 4.50);
    }
}
