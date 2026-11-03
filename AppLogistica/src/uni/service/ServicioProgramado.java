package uni.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.TipoServicio;

/** Mensajería programada: se entrega en la fecha acordada con el cliente. */
public class ServicioProgramado extends ServicioEnvioBase {

    @Override
    public TipoServicio getTipo() { return TipoServicio.PROGRAMADO; }

    @Override
    protected double getCargoFijo() { return 12.00; }

    @Override
    protected double getCostoPorKg() { return 2.20; }

    @Override
    protected int getDiasEntrega() { return 2; }

    @Override
    public int getPrioridad() { return 2; }

    @Override
    public boolean admite(EnvioTo envio) {
        return super.admite(envio) && envio.getFechaProgramada() != null;
    }

    @Override
    public LocalDate calcularFechaEstimada(EnvioTo envio, LocalDate fechaRegistro) {
        LocalDate programada = envio.getFechaProgramada();
        return (programada != null) ? programada : super.calcularFechaEstimada(envio, fechaRegistro);
    }

    @Override
    public List<EstadoEnvio> getFlujoEstados() {
        return Arrays.asList(EstadoEnvio.REGISTRADO, EstadoEnvio.RECOGIDO,
                EstadoEnvio.CLASIFICADO, EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO);
    }
}
