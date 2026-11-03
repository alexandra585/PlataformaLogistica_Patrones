package uni.service;

import java.util.Arrays;
import java.util.List;
import uni.entity.EstadoEnvio;
import uni.entity.TipoServicio;

/** Envío estándar: pasa por todas las etapas de consolidación. */
public class ServicioEstandar extends ServicioEnvioBase {

    @Override
    public TipoServicio getTipo() { return TipoServicio.ESTANDAR; }

    @Override
    protected double getCargoFijo() { return 10.00; }

    @Override
    protected double getCostoPorKg() { return 1.80; }

    @Override
    protected int getDiasEntrega() { return 3; }

    @Override
    public int getPrioridad() { return 3; }

    @Override
    public List<EstadoEnvio> getFlujoEstados() {
        return Arrays.asList(EstadoEnvio.REGISTRADO, EstadoEnvio.RECOGIDO,
                EstadoEnvio.EN_TRANSITO, EstadoEnvio.CLASIFICADO,
                EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO);
    }
}
