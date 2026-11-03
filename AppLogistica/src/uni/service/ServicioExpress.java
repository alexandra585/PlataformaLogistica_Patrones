package uni.service;

import java.util.Arrays;
import java.util.List;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.TipoServicio;

/** Entrega express: flujo corto, prioridad máxima, hasta 30 kg. */
public class ServicioExpress extends ServicioEnvioBase {

    public static final double PESO_MAXIMO = 30.0;

    @Override
    public TipoServicio getTipo() { return TipoServicio.EXPRESS; }

    @Override
    protected double getCargoFijo() { return 18.00; }

    @Override
    protected double getCostoPorKg() { return 3.50; }

    @Override
    protected int getDiasEntrega() { return 1; }

    @Override
    public int getPrioridad() { return 1; }

    @Override
    public boolean admite(EnvioTo envio) {
        return super.admite(envio) && envio.getPesoKg() <= PESO_MAXIMO;
    }

    @Override
    public List<EstadoEnvio> getFlujoEstados() {
        // Sin etapa de consolidación: va directo a ruta.
        return Arrays.asList(EstadoEnvio.REGISTRADO, EstadoEnvio.RECOGIDO,
                EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO);
    }
}
