package uni.service;

import java.time.LocalDate;
import java.util.List;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.TipoServicio;

/**
 * Producto del patrón Factory. Cada tipo de servicio encapsula sus reglas
 * de tiempo, costo, prioridad y flujo de estados (OCP + LSP).
 */
public interface IServicioEnvio {

    TipoServicio getTipo();

    /** Precondición explícita del contrato: ¿el servicio acepta este envío? */
    boolean admite(EnvioTo envio);

    double calcularTarifa(EnvioTo envio);

    LocalDate calcularFechaEstimada(EnvioTo envio, LocalDate fechaRegistro);

    List<EstadoEnvio> getFlujoEstados();

    /** 1 = máxima prioridad. */
    int getPrioridad();
}
