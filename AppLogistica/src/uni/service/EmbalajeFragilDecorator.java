package uni.service;

/** Embalaje y manipulación especial para contenido frágil: S/ 8.00. */
public class EmbalajeFragilDecorator extends TarifaDecorator {

    public EmbalajeFragilDecorator(ITarifa tarifa) {
        super(tarifa);
    }

    @Override
    public String getDescripcion() {
        return tarifa.getDescripcion() + " + Embalaje frágil";
    }

    @Override
    public double getMonto() {
        return redondear(tarifa.getMonto() + 8.00);
    }
}
