package uni.service;

import uni.entity.EnvioTo;

/** PATRÓN DECORATOR - ConcreteComponent: tarifa del servicio contratado. */
public class TarifaBase implements ITarifa {

    private final IServicioEnvio servicio;
    private final EnvioTo envio;

    public TarifaBase(IServicioEnvio servicio, EnvioTo envio) {
        this.servicio = servicio;
        this.envio = envio;
    }

    @Override
    public String getDescripcion() {
        return "Servicio " + servicio.getTipo() + " (" + envio.getPesoKg() + " kg)";
    }

    @Override
    public double getMonto() {
        return servicio.calcularTarifa(envio);
    }
}
