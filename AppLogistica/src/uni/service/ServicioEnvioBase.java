package uni.service;

import java.time.LocalDate;
import uni.entity.EnvioTo;

/**
 * Clase base de los servicios de envío. Define el algoritmo común de tarifa
 * (cargo fijo + costo por kilo) y deja a cada subclase sus parámetros.
 * Ninguna subclase lanza excepciones en estos métodos: las restricciones se
 * expresan en admite(), que forma parte del contrato (LSP).
 */
public abstract class ServicioEnvioBase implements IServicioEnvio {

    protected abstract double getCargoFijo();

    protected abstract double getCostoPorKg();

    protected abstract int getDiasEntrega();

    @Override
    public boolean admite(EnvioTo envio) {
        return envio != null && envio.getPesoKg() > 0 && envio.getDestino() != null;
    }

    @Override
    public double calcularTarifa(EnvioTo envio) {
        return redondear(getCargoFijo() + envio.getPesoKg() * getCostoPorKg());
    }

    @Override
    public LocalDate calcularFechaEstimada(EnvioTo envio, LocalDate fechaRegistro) {
        return fechaRegistro.plusDays(getDiasEntrega());
    }

    protected static double redondear(double monto) {
        return Math.round(monto * 100.0) / 100.0;
    }
}
