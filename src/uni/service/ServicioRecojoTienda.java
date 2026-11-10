package uni.service;

import java.util.Arrays;
import java.util.List;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.TipoServicio;

/**
 * Recojo en tienda: el destinatario retira el paquete en una agencia.
 * No tiene etapa EN_RUTA, pero cumple el mismo contrato que los demás
 * servicios (no lanza UnsupportedOperationException): LSP.
 */
public class ServicioRecojoTienda extends ServicioEnvioBase {

    @Override
    public TipoServicio getTipo() { return TipoServicio.RECOJO_TIENDA; }

    @Override
    protected double getCargoFijo() { return 6.00; }

    @Override
    protected double getCostoPorKg() { return 1.20; }

    @Override
    protected int getDiasEntrega() { return 2; }

    @Override
    public int getPrioridad() { return 4; }

    @Override
    public boolean admite(EnvioTo envio) {
        return envio != null && envio.getPesoKg() > 0 && envio.getIdtiendaRecojo() != null;
    }

    @Override
    public List<EstadoEnvio> getFlujoEstados() {
        return Arrays.asList(EstadoEnvio.REGISTRADO, EstadoEnvio.RECOGIDO,
                EstadoEnvio.EN_TRANSITO, EstadoEnvio.CLASIFICADO, EstadoEnvio.ENTREGADO);
    }
}
