package uni.service;

import java.util.Arrays;
import java.util.List;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.TipoServicio;

/** Transporte interprovincial: doble clasificación (origen y hub destino). */
public class ServicioInterprovincial extends ServicioEnvioBase {

    public static final double PESO_MAXIMO = 500.0;

    @Override
    public TipoServicio getTipo() { return TipoServicio.INTERPROVINCIAL; }

    @Override
    protected double getCargoFijo() { return 25.00; }

    @Override
    protected double getCostoPorKg() { return 2.50; }

    @Override
    protected int getDiasEntrega() { return 5; }

    @Override
    public int getPrioridad() { return 3; }

    @Override
    public boolean admite(EnvioTo envio) {
        return super.admite(envio) && envio.getPesoKg() <= PESO_MAXIMO;
    }

    @Override
    public List<EstadoEnvio> getFlujoEstados() {
        return Arrays.asList(EstadoEnvio.REGISTRADO, EstadoEnvio.RECOGIDO,
                EstadoEnvio.CLASIFICADO, EstadoEnvio.EN_TRANSITO,
                EstadoEnvio.CLASIFICADO, EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO);
    }
}
